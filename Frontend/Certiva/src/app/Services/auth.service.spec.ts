import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    localStorage.clear();
  });

  it('isLoggedIn es false sin token', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('hasRole reconoce rol sin prefijo ROLE_', () => {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(
      JSON.stringify({
        sub: 'test@certiva.com',
        rol: 'ROLE_MONITOR',
        exp: Math.floor(Date.now() / 1000) + 3600,
      })
    );
    localStorage.setItem('token', `${header}.${payload}.signature`);

    expect(service.getRol()).toBe('MONITOR');
    expect(service.hasRole('MONITOR')).toBeTrue();
    expect(service.isStaff()).toBeTrue();
    expect(service.isAdmin()).toBeFalse();
  });
});
