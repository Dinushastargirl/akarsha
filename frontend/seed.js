import { PrismaClient } from '@prisma/client';
import { PrismaPg } from '@prisma/adapter-pg';
import pg from 'pg';
import bcrypt from 'bcryptjs';
import 'dotenv/config';

const { Pool } = pg;
const pool = new Pool({ connectionString: process.env.DATABASE_URL });
const adapter = new PrismaPg(pool);
const prisma = new PrismaClient({ adapter });

async function main() {
    console.log("Seeding salons...");
    await prisma.salon.createMany({
        data: [
            { name: 'Salon Alpha', subdomain: 'alpha' },
            { name: 'Salon Beta', subdomain: 'beta' }
        ],
        skipDuplicates: true
    });

    console.log("Seeding users...");
    const passwordHash = await bcrypt.hash('Owner123!', 10);
    
    await prisma.user.createMany({
        data: [
            { tenantId: 'alpha', username: 'alpha_owner', email: 'owner@alpha.com', passwordHash, role: 'SALON_OWNER' },
            { tenantId: 'alpha', username: 'alpha_manager', email: 'manager@alpha.com', passwordHash, role: 'MANAGER' },
            { tenantId: 'beta', username: 'beta_owner', email: 'owner@beta.com', passwordHash, role: 'SALON_OWNER' }
        ],
        skipDuplicates: true
    });
    
    console.log("Database seeded successfully!");
}

main()
  .then(async () => {
    await prisma.$disconnect()
  })
  .catch(async (e) => {
    console.error(e)
    await prisma.$disconnect()
    process.exit(1)
  })
