import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-qr-code-display',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="qr-display">
      @if (data) {
        <img class="qr-img" [src]="qrUrl" [alt]="alt" [width]="size" [height]="size" loading="lazy" />
      }
      @if (caption) {
        <p class="qr-caption">{{ caption }}</p>
      }
      @if (showUrl && data) {
        <p class="qr-url text-break">{{ data }}</p>
      }
    </div>
  `,
  styles: [
    `
      .qr-display {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.5rem;
      }
      .qr-img {
        border-radius: 12px;
        background: #fff;
        padding: 8px;
        box-shadow: 0 4px 24px rgba(0, 0, 0, 0.35);
      }
      .qr-caption {
        margin: 0;
        font-size: 0.85rem;
        color: var(--text-secondary);
        text-align: center;
      }
      .qr-url {
        margin: 0;
        font-size: 0.72rem;
        color: var(--text-muted);
        max-width: 100%;
        word-break: break-all;
        text-align: center;
      }
    `,
  ],
})
export class QrCodeDisplayComponent {
  @Input() data = '';
  @Input() size = 200;
  @Input() alt = 'Código QR';
  @Input() caption = '';
  @Input() showUrl = false;

  get qrUrl(): string {
    const base = environment.API_URL.replace(/\/$/, '');
    return `${base}/public/qr?data=${encodeURIComponent(this.data)}&size=${this.size}`;
  }
}
