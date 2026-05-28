/** Etiquetas amigables para la UI (sin jerga de backend). */

export const ETIQUETAS_ROL: Record<string, string> = {
  ROLE_MONITOR: 'Monitor',
  MONITOR: 'Monitor',
  ROLE_PROFESOR: 'Profesor',
  PROFESOR: 'Profesor',
  ROLE_ADMIN: 'Administrador',
  ADMIN: 'Administrador',
  ROLE_ESTUDIANTE: 'Estudiante',
  ESTUDIANTE: 'Estudiante',
};

export const ETIQUETAS_TIPO_EVENTO: Record<string, string> = {
  CURSO: 'Curso',
  HACKATHON: 'Hackathon',
  TALLER: 'Taller',
  FERIA: 'Feria de proyectos',
};

export const ETIQUETAS_TIPO_CAMPO: Record<string, string> = {
  TEXTO: 'Texto',
  NUMERO: 'Número',
  SELECT: 'Lista desplegable',
  CHECKBOX: 'Sí / No',
  URL: 'URL / Enlace',
  IMAGEN: 'Imagen',
};

export function etiquetaTipoCampo(tipo: string | null | undefined): string {
  if (!tipo) {
    return '';
  }
  return ETIQUETAS_TIPO_CAMPO[tipo] ?? tipo;
}

export const ETIQUETAS_MODALIDAD: Record<string, string> = {
  PRESENCIAL: 'Presencial',
  VIRTUAL: 'Virtual',
  HIBRIDO: 'Híbrido',
};

export const ETIQUETAS_CATEGORIA_FERIA: Record<string, string> = {
  SOFTWARE: 'Software',
  HARDWARE: 'Hardware',
  INTELIGENCIA_ARTIFICIAL: 'Inteligencia artificial',
  INNOVACION_SOCIAL: 'Innovación social',
};

export const ETIQUETAS_NIVEL_CURSO: Record<string, string> = {
  BASICO: 'Básico',
  INTERMEDIO: 'Intermedio',
  AVANZADO: 'Avanzado',
};

export const STAFF_UI = {
  profesor: {
    titulo: 'Profesores colaboradores',
    placeholder: 'Buscar profesor / instructor…',
    hint: 'Selecciona el personal académico de apoyo para este evento.',
  },
  monitor: {
    titulo: 'Personal de apoyo (Staff)',
    placeholder: 'Buscar personal de apoyo…',
    hint: 'Personal operativo encargado del check-in en la entrada.',
  },
} as const;

export type TipoStaffUi = keyof typeof STAFF_UI;

export function normalizarCodigoRol(codigo: string | null | undefined): string {
  if (!codigo?.trim()) {
    return '';
  }
  let valor = codigo.trim().toUpperCase();
  while (valor.startsWith('ROLE_')) {
    valor = valor.slice(5);
  }
  return valor;
}

export function etiquetaRol(codigo: string | null | undefined): string {
  const normalizado = normalizarCodigoRol(codigo);
  if (!normalizado) {
    return '';
  }
  const conPrefijo = `ROLE_${normalizado}`;
  return ETIQUETAS_ROL[normalizado] ?? ETIQUETAS_ROL[conPrefijo] ?? humanizarCodigo(normalizado);
}

function humanizarCodigo(codigo: string): string {
  return codigo
    .replace(/^ROLE_/, '')
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/^\w/, c => c.toUpperCase());
}

export function etiquetaTipoEvento(tipo: string | null | undefined): string {
  if (!tipo) {
    return '';
  }
  return ETIQUETAS_TIPO_EVENTO[tipo] ?? tipo;
}

export function etiquetaModalidad(modalidad: string | null | undefined): string {
  if (!modalidad) {
    return '';
  }
  return ETIQUETAS_MODALIDAD[modalidad] ?? modalidad;
}

export function etiquetaCategoriaFeria(cat: string | null | undefined): string {
  if (!cat) {
    return '';
  }
  return ETIQUETAS_CATEGORIA_FERIA[cat] ?? cat;
}

export function etiquetaNivelCurso(nivel: string | null | undefined): string {
  if (!nivel) {
    return '';
  }
  return ETIQUETAS_NIVEL_CURSO[nivel] ?? nivel;
}
