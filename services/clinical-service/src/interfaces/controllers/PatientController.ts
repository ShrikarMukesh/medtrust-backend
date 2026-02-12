import { Request, Response } from 'express';
import { RegisterPatientUseCase } from '@application/use-cases/RegisterPatientUseCase';
import { RegisterPatientDTO } from '@application/dto/RegisterPatientDTO';

export class PatientController {
    constructor(private readonly registerPatientUseCase: RegisterPatientUseCase) { }

    async register(req: Request, res: Response): Promise<void> {
        try {
            const dto: RegisterPatientDTO = req.body;
            const patient = await this.registerPatientUseCase.execute(dto);

            res.status(201).json({
                success: true,
                data: {
                    id: patient.id,
                    mrn: patient.mrn,
                    firstName: patient.firstName,
                    lastName: patient.lastName,
                    dateOfBirth: patient.dateOfBirth.toISOString(),
                    gender: patient.gender,
                    contactInfo: patient.contactInfo,
                    createdAt: patient.createdAt.toISOString(),
                },
            });
        } catch (error: any) {
            res.status(400).json({
                success: false,
                message: error.message,
            });
        }
    }

    async getById(req: Request, res: Response): Promise<void> {
        try {
            // Delegates to a repository lookup — simplified for now
            res.status(501).json({ success: false, message: 'Not implemented yet' });
        } catch (error: any) {
            res.status(500).json({ success: false, message: error.message });
        }
    }
}
