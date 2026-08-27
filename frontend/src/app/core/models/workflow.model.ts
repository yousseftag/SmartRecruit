export type TemplateKey =
  | 'INTERVIEW_INVITATION'
  | 'FOLLOW_UP'
  | 'REJECTION'
  | 'OTHER'
  | (string & {});

export interface EmailTemplateResponse {
  id: string;
  templateKey: TemplateKey;
  name: string;
  subject: string;
  bodyHtml: string;
  updatedAt: string;
}

export interface CreateEmailTemplateRequest {
  name: string;
  subject: string;
  bodyHtml: string;
}

export interface UpdateEmailTemplateRequest {
  name: string;
  subject: string;
  bodyHtml: string;
}

export interface SendEmailRequest {
  subject: string;
  bodyHtml: string;
}
