import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { RolService } from './rol.service';

describe('RolService', () => {
  let service: RolService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(RolService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
