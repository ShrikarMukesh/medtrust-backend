import { Encounter } from '@domain/aggregates/Encounter';
import { IEncounterRepository } from '@domain/aggregates/IEncounterRepository';
import { IPatientRepository } from '@domain/entities/IPatientRepository';
import { CreateEncounterDTO } from '@application/dto/CreateEncounterDTO';

export class CreateEncounterUseCase {
    constructor(
        private readonly encounterRepository: IEncounterRepository,
        private readonly patientRepository: IPatientRepository,
    ) { }

    async execute(dto: CreateEncounterDTO): Promise<Encounter> {
        const patient = await this.patientRepository.findById(dto.patientId);
        if (!patient) {
            throw new Error(`Patient with ID ${dto.patientId} not found`);
        }

        const encounter = Encounter.create({
            patientId: dto.patientId,
        });

        return this.encounterRepository.save(encounter);
    }
}
