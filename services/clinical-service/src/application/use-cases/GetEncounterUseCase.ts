import { IEncounterRepository } from '@domain/aggregates/IEncounterRepository';
import { Encounter } from '@domain/aggregates/Encounter';

export class GetEncounterUseCase {
    constructor(private readonly encounterRepository: IEncounterRepository) { }

    async execute(encounterId: string): Promise<Encounter> {
        const encounter = await this.encounterRepository.findById(encounterId);
        if (!encounter) {
            throw new Error(`Encounter with ID ${encounterId} not found`);
        }
        return encounter;
    }
}
