import type { VercelRequest, VercelResponse } from '@vercel/node';
import { PrismaClient } from '@prisma/client';
import { PrismaPg } from '@prisma/adapter-pg';
import pg from 'pg';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';

const { Pool } = pg;
const pool = new Pool({ connectionString: process.env.POSTGRES_PRISMA_URL || process.env.DATABASE_URL });
const adapter = new PrismaPg(pool);
const prisma = new PrismaClient({ adapter });
const JWT_SECRET = process.env.JWT_SECRET || 'default-akarsha-super-secure-secret-key-that-is-at-least-256-bits-long';

export default async function handler(req: VercelRequest, res: VercelResponse) {
    // Handle CORS
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, Origin');

    if (req.method === 'OPTIONS') {
        res.status(200).end();
        return;
    }

    if (req.method === 'POST') {
        const { email, password } = req.body || {};
        
        if (!email || !password) {
            res.status(400).json({ message: "Email and password are required" });
            return;
        }

        try {
            // Find user in database
            const user = await prisma.user.findFirst({
                where: { email: email }
            });

            if (!user) {
                res.status(401).json({ message: "Invalid credentials" });
                return;
            }

            // Verify password
            const isPasswordValid = await bcrypt.compare(password, user.passwordHash);
            if (!isPasswordValid) {
                res.status(401).json({ message: "Invalid credentials" });
                return;
            }

            // Generate JWT token
            const token = jwt.sign(
                { sub: user.email, role: user.role, tenantId: user.tenantId },
                JWT_SECRET,
                { expiresIn: '24h' }
            );
            
            res.status(200).json({
                token: token,
                tenantId: user.tenantId,
                role: user.role
            });
            return;
        } catch (error) {
            console.error("Login error:", error);
            res.status(500).json({ message: "Internal server error" });
            return;
        }
    }

    res.status(405).json({ message: "Method not allowed" });
}
