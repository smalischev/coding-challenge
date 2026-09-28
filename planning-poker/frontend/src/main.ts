import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { BarController, BarElement, CategoryScale, LinearScale, Tooltip } from 'chart.js';
import { provideCharts } from 'ng2-charts';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/core/auth/auth.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor])),
    provideRouter(routes),
    provideCharts({ registerables: [BarController, BarElement, CategoryScale, LinearScale, Tooltip] }),
  ],
})
  .catch((error: unknown) => console.error(error));
