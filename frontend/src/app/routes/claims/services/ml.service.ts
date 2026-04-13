import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap } from 'rxjs';
import { environment } from '@env/environment';

export interface AcademicProfile {
  userId: number;
  age: number;
  g1: number;
  g2: number;
  failures: number;
  absences: number;
  studytime: number;
}

export interface MlPrediction {
  prediction: 'pass' | 'fail';
  probability_pass: number;
  probability_fail: number;
}

export interface MlAnalysis {
  prediction: MlPrediction;
  profile: { cluster: number; label: string; description: string };
}

@Injectable({ providedIn: 'root' })
export class MlService {
  private readonly http = inject(HttpClient);
  private readonly mlApi = environment.mlApiUrl;
  private readonly studentApi = environment.studentApiUrl;

  getAcademicProfile(userId: number): Observable<AcademicProfile> {
    return this.http.get<AcademicProfile>(
      `${this.studentApi}/api/students/${userId}/academic-profile`
    );
  }

  analyzeStudent(userId: number): Observable<MlAnalysis> {
    return this.getAcademicProfile(userId).pipe(
      switchMap(profile =>
        this.http.post<MlAnalysis>(`${this.mlApi}/analyze`, {
          G1: profile.g1,
          G2: profile.g2,
          age: profile.age,
          failures: profile.failures,
          absences: profile.absences,
          studytime: profile.studytime,
        })
      )
    );
  }
}