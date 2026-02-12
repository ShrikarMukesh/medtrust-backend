import { IEncounterRepository } from '@domain/aggregates/IEncounterRepository';
import { NoteType } from '@domain/entities/ClinicalNote';
import { AddClinicalNoteDTO } from '@application/dto/AddClinicalNoteDTO';
import { ClinicalNote } from '@domain/entities/ClinicalNote';

export class AddClinicalNoteUseCase {
    constructor(private readonly encounterRepository: IEncounterRepository) { }

    async execute(dto: AddClinicalNoteDTO): Promise<ClinicalNote> {
        const encounter = await this.encounterRepository.findById(dto.encounterId);
        if (!encounter) {
            throw new Error(`Encounter with ID ${dto.encounterId} not found`);
        }

        const note = encounter.addNote({
            content: dto.content,
            authorId: dto.authorId,
            noteType: dto.noteType as NoteType,
        });

        await this.encounterRepository.save(encounter);

        return note;
    }
}
