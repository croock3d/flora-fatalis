import {
  APP_INITIALIZER,
  ApplicationConfig,
  inject,
  isDevMode,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter, withViewTransitions } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideServiceWorker } from '@angular/service-worker';

import { routes } from './app.routes';
import { authInterceptor } from './auth/auth.interceptor';
import { AuthService } from './auth/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(
      routes,
      withViewTransitions({
        onViewTransitionCreated: ({ transition, to, from }) => {
          const toPath = to.url.map((s) => s.path).join('/');
          const fromPath = from.url.map((s) => s.path).join('/');
          if (toPath === fromPath) {
            transition.skipTransition();
          }
        },
      }),
    ),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideServiceWorker('ngsw-worker.js', {
      enabled: !isDevMode(),
      registrationStrategy: 'registerWhenStable:30000',
    }),
    {
      provide: APP_INITIALIZER,
      useFactory: () => {
        inject(AuthService);
        return () => {};
      },
      multi: true,
    },
  ],
};
