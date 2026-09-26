export type ApiRole = 'DEVELOPER' | 'SCRUM_MASTER';
export type BackendCardValue = 'ZERO' | 'ONE' | 'TWO' | 'THREE' | 'FIVE' | 'EIGHT' | 'THIRTEEN' | 'TWENTY_ONE' | 'THIRTY_FOUR' | 'QUESTION_MARK' | 'COFFEE';
export type PlanningPokerId = string;

export interface Credentials { username: string; password: string; role?: ApiRole; }
export interface TokenResponse { accessToken: string; tokenType: string; expiresIn: number; }
export interface CreateSessionRequest { scrumMasterName: string; gitlabProjectId: number; gitlabIssueIid: number; }
// Implemented by the backend, but currently missing from the generated OpenAPI schema.
export interface CreateSessionResponse { planningPokerId: PlanningPokerId; }
export interface DeveloperRequest { developerName: string; }
export interface ScrumMasterRequest { scrumMasterName: string; }
export interface SelectIssueRequest { scrumMasterName: string; gitlabIssueIid: number; }
export interface EstimateRequest { developerName: string; value: BackendCardValue; }
export interface FinalizeResultRequest { scrumMasterName: string; value: BackendCardValue; }
export interface ActiveIssueResponse { gitlabIssueIid: number; title: string; description: string; }
export interface EstimationProgressResponse { estimatedDevelopers: string[]; pendingDevelopers: string[]; }
export interface AllDevelopersEstimatedResponse { allDevelopersEstimated: boolean; }
export interface EstimateValueResponse { developerName: string; value: BackendCardValue; }
export interface NumericEstimationResponse { value: number | null; }
