import {
  ApplicationConfig,
  importProvidersFrom,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import {
  Check,
  ChevronDown,
  ChevronUp,
  CircleSlash,
  Clock,
  Code,
  Download,
  Flame,
  FlaskConical,
  CircleQuestionMark,
  Inbox,
  LibraryBig,
  Lightbulb,
  LogOut,
  LucideAngularModule,
  MessageSquare,
  Plus,
  Search,
  ShieldCheck,
  Swords,
  Tag,
  TrendingUp,
  User,
  X,
} from 'lucide-angular';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    importProvidersFrom(
      LucideAngularModule.pick({
        Check,
        ChevronDown,
        ChevronUp,
        CircleSlash,
        Clock,
        Code,
        Download,
        Flame,
        FlaskConical,
        CircleQuestionMark,
        Inbox,
        LibraryBig,
        Lightbulb,
        LogOut,
        MessageSquare,
        Plus,
        Search,
        ShieldCheck,
        Swords,
        Tag,
        TrendingUp,
        User,
        X,
      }),
    ),
  ],
};
