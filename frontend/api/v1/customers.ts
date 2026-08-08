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
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, Origin');

    if (req.method === 'OPTIONS') {
        res.status(200).end();
        return;
    }

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

    const tenantId = decoded.tenantId;

    try {
        if (req.method === 'GET') {
            const page = parseInt(req.query.page as string || '0');
            const size = parseInt(req.query.size as string || '10');

            const [customers, totalElements] = await Promise.all([
                prisma.customer.findMany({
                    where: { tenantId },
                    skip: page * size,
                    take: size,
                    orderBy: { createdAt: 'desc' }
                }),
                prisma.customer.count({ where: { tenantId } })
            ]);

            // Convert BigInts to strings for JSON serialization
            const serializedCustomers = customers.map(c => ({
                ...c,
                id: c.id.toString()
            }));

            res.status(200).json({
                content: serializedCustomers,
                totalPages: Math.ceil(totalElements / size),
                totalElements: totalElements,
                size: size,
                number: page
            });
            return;
        }

        res.status(405).json({ message: "Method not allowed" });
    } catch (error) {
        console.error("Customers API error:", error);
        res.status(500).json({ message: "Internal server error" });
    }
}
