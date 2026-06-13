export type UserRole = 'USER' | 'ADMIN';

export interface CurrentUser {
  email: string;
  displayName: string;
  role: UserRole;
  currentStreak: number;
  logoutUrl: string | null;
}
