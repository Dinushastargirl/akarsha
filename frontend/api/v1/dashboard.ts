import type { VercelRequest, VercelResponse } from '@vercel/node';
import { PrismaClient } from '@prisma/client';
import { PrismaPg } from '@prisma/adapter-pg';
import pg from 'pg';
import jwt from 'jsonwebtoken';

const { Pool } = pg;
const pool = new Pool({ connectionString: process.env.POSTGRES_PRISMA_URL || process.env.DATABASE_URL });
const adapter = new PrismaPg(pool);
const prisma = new PrismaClient({ adapter });
const JWT_SECRET = process.env.JWT_SECRET || 'default-akarsha-super-secure-secret-key-that-is-at-least-256-bits-long';

export default async function handler(req: VercelRequest, res: VercelResponse) {
    // Handle CORS
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, Origin');

    if (req.method === 'OPTIONS') {
        res.status(200).end();
        return;
    }

    if (req.method !== 'GET') {
        res.status(405).json({ message: "Method not allowed" });
        return;
    }

    // Auth verification
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        res.status(401).json({ message: "Unauthorized" });
        return;
    }

    const token = authHeader.split(' ')[1];
    let decoded: any;
    try {
        decoded = jwt.verify(token, JWT_SECRET);
    } catch (e) {
        res.status(401).json({ message: "Invalid token" });
        return;
    }

    try {
        // Query database
        const totalCustomers = await prisma.customer.count();
        const activeStaff = await prisma.user.count(); // For now, just count all users
        const activeServices = await prisma.service.count();
        
        // Return mostly mock data for appointments since we haven't seeded them yet
        res.status(200).json({
            todayTotal: 0,
            todayCompleted: 0,
            todayCancelled: 0,
            todayEstimatedRevenue: 0,
            totalCustomers,
            activeStaff,
            activeServices,
            todayTimeline: [],
            upcomingAppointments: []
        });
    } catch (error) {
        console.error("Dashboard error:", error);
        res.status(500).json({ message: "Internal server error" });
    }
}
