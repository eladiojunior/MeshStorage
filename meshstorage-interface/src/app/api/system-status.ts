import type { NextApiRequest, NextApiResponse } from 'next';

export default function handler(req: NextApiRequest, res: NextApiResponse) {
    console.log(req);
    res.status(200).json({
        status: 'OK',
        timestamp: new Date().toISOString(),
        message: 'Todos os sistemas operacionais estão funcionando',
    });
}