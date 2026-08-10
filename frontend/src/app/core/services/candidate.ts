import { Injectable } from '@angular/core';
import { Observable, of, delay, tap } from 'rxjs';
import { Application, CvFile } from '../models/candidate.model';

@Injectable({
  providedIn: 'root',
})
export class CandidateService {
  
  // MOCKING NLP RESULTS for Frontend Development
  mockUploadAndPoll(offerId: string, files: File[]): Observable<Application[]> {
    // Generate mock PENDING applications
    const pendingApps: Application[] = files.map((file, index) => ({
      id: `mock-app-${Date.now()}-${index}`,
      candidate_id: `mock-cand-${Date.now()}`,
      offer_id: offerId,
      status: 'NEW',
      applied_at: new Date().toISOString(),
    }));

    return of(pendingApps).pipe(
      tap(() => console.log('Simulating upload and returning PENDING status...'))
    );
  }

  // Simulate short polling that resolves after 3 seconds
  mockPollExtractionStatus(appId: string): Observable<Application> {
    const successApp: Application = {
      id: appId,
      candidate_id: `mock-cand-${Date.now()}`,
      offer_id: 'mock-offer-id',
      status: 'NEW',
      applied_at: new Date().toISOString(),
      total_score: 85.0,
      category_scores: {
        skills: 35.0, // out of 40
        experience: 20.0, // out of 20
        coursework: 20.0, // out of 20
        languages: 10.0, // out of 10
        localization: 0.0 // out of 10
      },
      extracted_matching: {
        matched_criteria: {
          skills: ["Java 17", "Spring Boot", "Angular", "Docker", "Kubernetes", "PostgreSQL"],
          experience: true,
          coursework: ["Master's Degree in Computer Science"],
          languages: ["English", "French"],
          localization: "Casablanca" // Mismatch, expected Agadir
        },
        strengths: [
          "Perfect match for Technical Skills (Java, Spring Boot, Angular)",
          "Exceeds required experience (60 months vs 36 required)",
          "Fluent in required languages"
        ],
        weaknesses: [
          "Candidate is based in Casablanca, offer requires Agadir (localization mismatch)"
        ]
      },
      cv_file: {
        id: `mock-cv-${Date.now()}`,
        extraction_status: 'SUCCESS',
        original_filename: 'youssef_resume_2026.pdf',
        extracted_data: {
          candidate_info: {
            first_name: "Youssef",
            last_name: "Alaoui",
            email: "youssef.alaoui@example.com",
            phone: "+212 600-000000"
          },
          description_markdown: "Senior Full-Stack Developer with 5 years of experience specializing in Java/Spring Boot and Angular. Proven track record of delivering scalable enterprise applications and leading agile teams.",
          skills: ["Java 17", "Spring Boot", "Angular", "TypeScript", "Docker", "Kubernetes", "AWS", "PostgreSQL", "CI/CD"],
          experience: 60, // 5 years
          coursework: ["Master in Software Engineering - INPT", "Bachelor in Computer Science"],
          languages: ["Arabic (Native)", "French (Bilingual)", "English (Professional)"],
          localization: "Casablanca, Morocco"
        }
      }
    };

    return of(successApp).pipe(
      delay(3000), // Simulate 3 seconds of RabbitMQ/FastAPI processing
      tap(() => console.log(`Extraction SUCCESS for ${appId}`))
    );
  }
}
