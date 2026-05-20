import { TipoEventoEnum } from '../Models/evento-dto';

/** Imágenes por defecto cuando el evento no tiene imagen promocional en BD. */
export const IMAGEN_EVENTO_POR_TIPO: Record<TipoEventoEnum, string> = {
  CURSO:
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=800&q=80',
  HACKATHON:
    'https://images.unsplash.com/photo-1517694712202-008ef8b79503?auto=format&fit=crop&w=800&q=80',
  TALLER:
    'https://images.unsplash.com/photo-1524178232363-1fb2b075b655?auto=format&fit=crop&w=800&q=80',
  FERIA:
    'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=800&q=80',
};

export const HERO_CATALOGO_IMAGEN =
  'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1600&q=80';

export type FiltroCategoriaCatalogo = 'Todos' | 'Cursos' | 'Hackathones' | 'Ferias' | 'Talleres';

export const CHIPS_CATALOGO: { id: FiltroCategoriaCatalogo; label: string; tipo?: TipoEventoEnum }[] = [
  { id: 'Todos', label: 'Todos los eventos' },
  { id: 'Cursos', label: 'Cursos', tipo: 'CURSO' },
  { id: 'Hackathones', label: 'Hackathones', tipo: 'HACKATHON' },
  { id: 'Ferias', label: 'Ferias', tipo: 'FERIA' },
  { id: 'Talleres', label: 'Talleres', tipo: 'TALLER' },
];

export const FAVORITOS_STORAGE_KEY = 'certiva_eventos_favoritos';
