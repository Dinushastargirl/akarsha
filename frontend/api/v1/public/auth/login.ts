import type { VercelRequest, VercelResponse } from '@vercel/node';

export default function handler(req: VercelRequest, res: VercelResponse) {
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
        
        // Mock successful login for the owner
        if (email === 'owner@alpha.com' && password === 'Owner123!') {
            // Generate a fake JWT token
            const token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvd25lckBhbHBoYS5jb20iLCJyb2xlIjoiU0FMT05fT1dORVIiLCJ0ZW5hbnRJZCI6ImFscGhhIiwiaWF0IjoxNzEyMTIzNDU2LCJleHAiOjE3MTIxMjcwNTZ9.THIS_IS_A_MOCK_TOKEN_FOR_VERCEL";
            
            res.status(200).json({
                token: token,
                tenantId: "alpha",
                role: "SALON_OWNER"
            });
            return;
        }

        res.status(401).json({ message: "Invalid credentials" });
        return;
    }

    res.status(405).json({ message: "Method not allowed" });
}
