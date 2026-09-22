import { apiFetch, SERVICE_URLS, isMockMode } from '../api';

export interface IntegrationMessageResponse {
  id: string;
  direction: 'INBOUND' | 'OUTBOUND';
  resourceType: string;
  externalSystemId: string;
  correlationId: string;
  status: 'RECEIVED' | 'PROCESSING' | 'PROCESSED' | 'FAILED';
  errorMessage?: string;
  internalResourceId?: string;
  createdAt: string;
  processedAt?: string;
  updatedAt: string;
}

export interface InboundFhirRequest {
  externalSystemId: string;
  resourceType: string;
  correlationId: string;
  fhirPayload: string;
}

export interface OutboundFhirRequest {
  externalSystemId: string;
  resourceType: string;
  internalResourceId: string;
  fhirPayload: string;
}

const BASE = SERVICE_URLS.integration;

const mockIntegrations: IntegrationMessageResponse[] = [
  {
    id: 'int-1',
    direction: 'INBOUND',
    resourceType: 'Patient',
    externalSystemId: 'EPIC-EHR-01',
    correlationId: 'corr-epic-9821',
    status: 'PROCESSED',
    internalResourceId: 'pat-101',
    createdAt: new Date(Date.now() - 14400000).toISOString(),
    processedAt: new Date(Date.now() - 14350000).toISOString(),
    updatedAt: new Date(Date.now() - 14350000).toISOString(),
  },
  {
    id: 'int-2',
    direction: 'OUTBOUND',
    resourceType: 'Encounter',
    externalSystemId: 'CERNER-HOSP-02',
    correlationId: 'corr-crn-4102',
    status: 'PROCESSED',
    internalResourceId: 'enc-202',
    createdAt: new Date(Date.now() - 7200000).toISOString(),
    processedAt: new Date(Date.now() - 7190000).toISOString(),
    updatedAt: new Date(Date.now() - 7190000).toISOString(),
  },
];

export async function getIntegrations(): Promise<IntegrationMessageResponse[]> {
  if (isMockMode()) return mockIntegrations;
  return apiFetch<IntegrationMessageResponse[]>(BASE, '/api/integration');
}

export async function getIntegration(id: string): Promise<IntegrationMessageResponse> {
  if (isMockMode()) {
    const item = mockIntegrations.find(x => x.id === id);
    if (!item) throw new Error('Integration message not found');
    return item;
  }
  return apiFetch<IntegrationMessageResponse>(BASE, `/api/integration/${id}`);
}

export async function processInboundFhir(data: InboundFhirRequest): Promise<IntegrationMessageResponse> {
  if (isMockMode()) {
    const item: IntegrationMessageResponse = {
      id: `int-${Date.now()}`,
      direction: 'INBOUND',
      resourceType: data.resourceType,
      externalSystemId: data.externalSystemId,
      correlationId: data.correlationId,
      status: 'PROCESSED',
      createdAt: new Date().toISOString(),
      processedAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    mockIntegrations.unshift(item);
    return item;
  }
  return apiFetch<IntegrationMessageResponse>(BASE, '/api/integration/fhir/inbound', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function processOutboundFhir(data: OutboundFhirRequest): Promise<IntegrationMessageResponse> {
  if (isMockMode()) {
    const item: IntegrationMessageResponse = {
      id: `int-${Date.now()}`,
      direction: 'OUTBOUND',
      resourceType: data.resourceType,
      externalSystemId: data.externalSystemId,
      correlationId: `corr-${Date.now()}`,
      internalResourceId: data.internalResourceId,
      status: 'PROCESSED',
      createdAt: new Date().toISOString(),
      processedAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    mockIntegrations.unshift(item);
    return item;
  }
  return apiFetch<IntegrationMessageResponse>(BASE, '/api/integration/fhir/outbound', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
