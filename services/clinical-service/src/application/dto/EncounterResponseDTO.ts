export interface DiagnosisDTO {
    code: string;
    description: string;
}

export interface ClinicalNoteResponseDTO {
    id: string;
    content: string;
    authorId: string;
    noteType: string;
    createdAt: string;
}

export interface EncounterResponseDTO {
    id: string;
    patientId: string;
    status: string;
    startDate: string;
    endDate: string | null;
    clinicalNotes: ClinicalNoteResponseDTO[];
    diagnoses: DiagnosisDTO[];
    createdAt: string;
    updatedAt: string;
}
