import { v4 as uuidv4 } from 'uuid';

export enum NoteType {
    PROGRESS = 'PROGRESS',
    ADMISSION = 'ADMISSION',
    DISCHARGE = 'DISCHARGE',
    CONSULTATION = 'CONSULTATION',
    PROCEDURE = 'PROCEDURE',
}

export interface ClinicalNoteProps {
    id?: string;
    encounterId: string;
    content: string;
    authorId: string;
    noteType: NoteType;
    createdAt?: Date;
}

export class ClinicalNote {
    readonly id: string;
    readonly encounterId: string;
    readonly content: string;
    readonly authorId: string;
    readonly noteType: NoteType;
    readonly createdAt: Date;

    private constructor(props: Required<ClinicalNoteProps>) {
        this.id = props.id;
        this.encounterId = props.encounterId;
        this.content = props.content;
        this.authorId = props.authorId;
        this.noteType = props.noteType;
        this.createdAt = props.createdAt;
    }

    static create(props: ClinicalNoteProps): ClinicalNote {
        if (!props.content || props.content.trim().length === 0) {
            throw new Error('Clinical note content cannot be empty');
        }

        return new ClinicalNote({
            id: props.id ?? uuidv4(),
            encounterId: props.encounterId,
            content: props.content,
            authorId: props.authorId,
            noteType: props.noteType,
            createdAt: props.createdAt ?? new Date(),
        });
    }
}
