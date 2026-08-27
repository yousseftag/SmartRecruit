import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateEmailTemplateRequest,
  EmailTemplateResponse,
  SendEmailRequest,
  UpdateEmailTemplateRequest,
} from '../models/workflow.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class WorkflowService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/v1/workflow`;

  /** Fetches all email templates */
  getTemplates(): Observable<EmailTemplateResponse[]> {
    return this.http.get<EmailTemplateResponse[]>(`${this.apiUrl}/templates`);
  }

  /** Fetches a single email template by its UUID */
  getTemplateById(id: string): Observable<EmailTemplateResponse> {
    return this.http.get<EmailTemplateResponse>(`${this.apiUrl}/templates/${id}`);
  }

  /** Creates a new custom email template */
  createTemplate(request: CreateEmailTemplateRequest): Observable<EmailTemplateResponse> {
    return this.http.post<EmailTemplateResponse>(`${this.apiUrl}/templates`, request);
  }

  /** Updates an existing email template by its UUID */
  updateTemplate(
    id: string,
    request: UpdateEmailTemplateRequest,
  ): Observable<EmailTemplateResponse> {
    return this.http.put<EmailTemplateResponse>(`${this.apiUrl}/templates/${id}`, request);
  }

  /** Deletes a custom email template by its UUID */
  deleteTemplate(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/templates/${id}`);
  }

  /** Dispatches an email to the candidate linked to the application */
  sendEmail(applicationId: string, request: SendEmailRequest): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/applications/${applicationId}/send-email`,
      request,
    );
  }
}
