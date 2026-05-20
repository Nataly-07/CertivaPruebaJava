import { TipoEventoEnum } from '../Models/evento-dto';

export interface TarjetaTipoEventoConfig {
  tipo: TipoEventoEnum;
  categoria: string;
  categoriaClass: string;
  titulo: string;
  descripcion: string;
  progressClass: string;
  footerIcon: string;
  footerRegla: string;
  counterSingular: string;
  counterPlural: string;
}

export const TARJETAS_TIPO_EVENTO: TarjetaTipoEventoConfig[] = [
  {
    tipo: 'CURSO',
    categoria: 'ACADÉMICO',
    categoriaClass: 'cat-curso',
    titulo: 'Curso',
    descripcion:
      'Formación estructurada con criterios de aprobación, asistencia mínima y certificación académica.',
    progressClass: 'prog-curso',
    footerIcon: 'bi-mortarboard',
    footerRegla: 'Requiere Nota Mínima y Asistencia',
    counterSingular: 'Curso',
    counterPlural: 'Cursos',
  },
  {
    tipo: 'HACKATHON',
    categoria: 'DESAFÍO',
    categoriaClass: 'cat-hackathon',
    titulo: 'Hackathon',
    descripcion:
      'Competencia intensiva por equipos con retos técnicos, integrantes mínimos/máximos y premios.',
    progressClass: 'prog-hackathon',
    footerIcon: 'bi-code-slash',
    footerRegla: 'Gestión de Retos y Equipos',
    counterSingular: 'Hack',
    counterPlural: 'Hacks',
  },
  {
    tipo: 'FERIA',
    categoria: 'EXHIBICIÓN',
    categoriaClass: 'cat-feria',
    titulo: 'Feria de Proyectos',
    descripcion:
      'Espacio de exhibición con categorías, stack tecnológico y criterios de evaluación avanzados.',
    progressClass: 'prog-feria',
    footerIcon: 'bi-diagram-3',
    footerRegla: 'Criterios de Evaluación Avanzados',
    counterSingular: 'Feria',
    counterPlural: 'Ferias',
  },
  {
    tipo: 'TALLER',
    categoria: 'PRÁCTICO',
    categoriaClass: 'cat-taller',
    titulo: 'Taller',
    descripcion:
      'Sesiones prácticas orientadas a habilidades con material guía y certificación por intensidad horaria.',
    progressClass: 'prog-taller',
    footerIcon: 'bi-lightning-charge',
    footerRegla: 'Certificación por Intensidad Horaria',
    counterSingular: 'Taller',
    counterPlural: 'Talleres',
  },
];
