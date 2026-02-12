import { Request, Response } from 'express';
import { CreateEncounterUseCase } from '@application/use-cases/CreateEncounterUseCase';
import { AddClinicalNoteUseCase } from '@application/use-cases/AddClinicalNoteUseCase';
import { GetEncounterUseCase } from '@application/use-cases/GetEncounterUseCase';
import { EncounterResponseDTO } from '@application/dto/EncounterResponseDTO';
import { Encounter } from '@domain/aggregates/Encounter';

export class EncounterController {
    constructor(
        private readonly createEncounterUseCase: CreateEncounterUseCase,
        private readonly addClinicalNoteUseCase: AddClinicalNoteUseCase,
        private readonly getEncounterUseCase: GetEncounterUseCase,
    ) { }

    async create(req: Request, res: Response): Promise<void> {
        try {
            const encounter = await this.createEncounterUseCase.execute(req.body);
            res.status(201).json({
                success: true,
                data: this.toResponseDTO(encounter),
            });
        } catch (error: any) {
            res.status(400).json({ success: false, message: error.message });
        }
    }

    async getById(req: Request, res: Response): Promise<void> {
        try {
            const encounter = await this.getEncounterUseCase.execute(req.params.id as string);
            res.status(200).json({
                success: true,
                data: this.toResponseDTO(encounter),
            });
        } catch (error: any) {
            res.status(404).json({ success: false, message: error.message });
        }
    }

    async addNote(req: Request, res: Response): Promise<void> {
        try {
            const note = await this.addClinicalNoteUseCase.execute({
                encounterId: req.params.id,
                ...req.body,
            });

            res.status(201).json({
                success: true,
                data: {
                    id: note.id,
                    encounterId: note.encounterId,
                    content: note.content,
                    authorId: note.authorId,
                    noteType: note.noteType,
                    createdAt: note.createdAt.toISOString(),
                },
            });
        } catch (error: any) {
            res.status(400).json({ success: false, message: error.message });
        }
    }

    private toResponseDTO(encounter: Encounter): EncounterResponseDTO {
        return {
            id: encounter.id,
            patientId: encounter.patientId,
            status: encounter.status,
            startDate: encounter.startDate.toISOString(),
            endDate: encounter.endDate?.toISOString() ?? null,
            clinicalNotes: encounter.clinicalNotes.map((n) => ({
                id: n.id,
                content: n.content,
                authorId: n.authorId,
                noteType: n.noteType,
                createdAt: n.createdAt.toISOString(),
            })),
            diagnoses: encounter.diagnoses.map((d) => ({
                code: d.getCode(),
                description: d.getDescription(),
            })),
            createdAt: encounter.createdAt.toISOString(),
            updatedAt: encounter.updatedAt.toISOString(),
        };
    }
}
