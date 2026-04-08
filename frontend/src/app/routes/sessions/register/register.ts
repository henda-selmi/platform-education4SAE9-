import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { MtxButtonModule } from '@ng-matero/extensions/button';
import { AuthService } from '@core/authentication';
import { LoginService } from '@core/authentication';

@Component({
  selector: 'app-register',
  templateUrl: './register.html',
  styleUrl: './register.scss',
  imports: [
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MtxButtonModule,
  ],
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly loginService = inject(LoginService);
  private readonly snackBar = inject(MatSnackBar);

  isSubmitting = false;

  registerForm = this.fb.nonNullable.group(
    {
      firstName:       ['', [Validators.required]],
      lastName:        ['', [Validators.required]],
      email:           ['', [Validators.required, Validators.email]],
      password:        ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      role:            ['STUDENT', [Validators.required]],
    },
    { validators: [this.matchValidator('password', 'confirmPassword')] }
  );

  matchValidator(source: string, target: string) {
    return (control: AbstractControl) => {
      const src = control.get(source)!;
      const tgt = control.get(target)!;
      if (tgt.errors && !tgt.errors['mismatch']) return null;
      if (src.value !== tgt.value) {
        tgt.setErrors({ mismatch: true });
        return { mismatch: true };
      }
      tgt.setErrors(null);
      return null;
    };
  }

  register() {
    if (this.registerForm.invalid) return;
    this.isSubmitting = true;

    const { firstName, lastName, email, password, role } = this.registerForm.getRawValue();

    this.loginService.register({ firstName, lastName, email, password, role }).subscribe({
      next: token => {
        // Store token and log in automatically
        this.authService.login(email, password).subscribe({
          next: () => this.router.navigateByUrl('/'),
          error: () => this.router.navigateByUrl('/auth/login'),
        });
      },
      error: (err: HttpErrorResponse) => {
        const msg = err.error?.message || 'Registration failed. Please try again.';
        this.snackBar.open(msg, 'Close', { duration: 4000 });
        this.isSubmitting = false;
      },
    });
  }
}
