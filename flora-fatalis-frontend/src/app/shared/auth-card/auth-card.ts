import { Component, input } from '@angular/core';

@Component({
  selector: 'app-auth-card',
  templateUrl: './auth-card.html',
  styleUrl: './auth-card.css',
})
export class AuthCardComponent {
  readonly subtitle = input.required<string>();
}
