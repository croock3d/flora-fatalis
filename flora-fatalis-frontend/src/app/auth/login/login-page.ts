import { Component, inject, isDevMode, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../auth.service';
import { AuthCardComponent } from '../../shared/auth-card/auth-card';

const DEV_EMAIL = 'dev@flora-fatalis.local';
const DEV_PASSWORD = 'devpass';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink, AuthCardComponent],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  readonly errorMessage = signal<string | null>(null);
  readonly isLoading = signal(false);
  readonly isDev = isDevMode();

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, password } = this.form.getRawValue();

    this.authService.login({ email, password }).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.errorMessage.set('Nieprawidłowe dane logowania');
        } else {
          this.errorMessage.set('Wystąpił błąd. Spróbuj ponownie.');
        }
      },
    });
  }

  loginAsDev(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.authService.login({ email: DEV_EMAIL, password: DEV_PASSWORD }).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: () => this.isLoading.set(false),
    });
  }

  isFieldInvalid(field: 'email' | 'password'): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control.touched);
  }
}
