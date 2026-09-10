import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../auth.service';
import { AuthCardComponent } from '../../shared/auth-card/auth-card';

const passwordsMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordsMismatch: true };
};

@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink, AuthCardComponent],
  templateUrl: './register-page.html',
  styleUrl: '../login/login-page.css',
})
export class RegisterPage {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.nonNullable.group(
    {
      displayName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatchValidator },
  );

  readonly errorMessage = signal<string | null>(null);
  readonly isLoading = signal(false);

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { displayName, email, password } = this.form.getRawValue();

    this.authService.register({ displayName, email, password }).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 409) {
          this.errorMessage.set('Ten email jest już zajęty');
        } else {
          this.errorMessage.set('Wystąpił błąd. Spróbuj ponownie.');
        }
      },
    });
  }

  isFieldInvalid(field: 'displayName' | 'email' | 'password' | 'confirmPassword'): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control.touched);
  }

  get passwordsMismatch(): boolean {
    return !!(
      this.form.hasError('passwordsMismatch') &&
      this.form.get('confirmPassword')?.touched
    );
  }
}
