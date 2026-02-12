import { Router } from 'express';
import { EncounterController } from '@interfaces/controllers/EncounterController';

export function createEncounterRoutes(controller: EncounterController): Router {
    const router = Router();

    router.post('/', (req, res) => controller.create(req, res));
    router.get('/:id', (req, res) => controller.getById(req, res));
    router.post('/:id/notes', (req, res) => controller.addNote(req, res));

    return router;
}
