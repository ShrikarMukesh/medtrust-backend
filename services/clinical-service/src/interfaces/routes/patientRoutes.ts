import { Router } from 'express';
import { PatientController } from '@interfaces/controllers/PatientController';

export function createPatientRoutes(controller: PatientController): Router {
    const router = Router();

    router.post('/', (req, res) => controller.register(req, res));
    router.get('/:id', (req, res) => controller.getById(req, res));

    return router;
}
