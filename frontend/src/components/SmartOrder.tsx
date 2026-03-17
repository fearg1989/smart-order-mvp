import React, { useState } from 'react';
import { useOrderProcessor } from '../hooks/useOrderProcessor';
import type { OrderRequest } from '../types/order';
import { Light as SyntaxHighlighter } from 'react-syntax-highlighter';
import { github } from 'react-syntax-highlighter/dist/esm/styles/hljs';
import json from 'highlight.js/lib/languages/json.js';

SyntaxHighlighter.registerLanguage('json', json);

const DEFAULT_CLIENT: Pick<OrderRequest, 'clientId' | 'clientName' | 'clientEmail'> = {
  clientId: 'client-web',
  clientName: 'Web Client',
  clientEmail: 'noreply@smart-order.local',
};

const BILLING_MARKER_REGEX = /\b(?:bill\s*to|billing\s*to|invoice\s*to|facturar\s*a(?:\s+nombre\s+de)?|factura\s*a(?:\s+nombre\s+de)?|a\s+nombre\s+de)\b/i;
const BILLING_LINE_REGEX = /(?:^|\n)\s*(?:bill\s*to|billing\s*to|invoice\s*to|facturar\s*a(?:\s+nombre\s+de)?|factura\s*a(?:\s+nombre\s+de)?|a\s+nombre\s+de)\s*[:\-]?\s*(.+)\s*$/im;

const extractBillingClientName = (text: string): string | null => {
  const match = text.match(BILLING_LINE_REGEX);
  if (!match?.[1]) return null;
  const normalized = match[1].trim().replace(/[\s.;,]+$/, '');
  return normalized || null;
};

const formatEur = (amount: number) =>
  new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(amount);

const EXAMPLES = [
  {
    mode: 'online',
    label: "Normal",
    icon: "📝",
    text: "Hola Antonio, te paso el pedido de reposición para la tienda de Valencia de la campaña de primavera:\n- 24 pares Bota Chelsea Piel (Ref: BT-09) - Surtido de tallas 38 al 42\n- 12 pares Zapato Salón Negro (Ref: ZS-01) - Tallas 37 y 38\n- 50 Cinturones Cuero Básicos (Ref: CIN-05)\nEnvíalo por SEUR como siempre. Facturar a Calzados Levante S.L."
  },
  {
    mode: 'online',
    label: "WhatsApp Caótico",
    icon: "💬",
    text: "Paco pasame urgente para el semillero 15 sacos del abono nitrato 25kg (el barato de la otra vez), 5 palets de cajas de carton para la pera y 2 rollos de plastico de invernadero. apuntamelo a la cuenta de la finca sur, el NIF ya lo teneis. avisame cuando el camion salga q no hay nadie en la nave"
  },
  {
    mode: 'online',
    label: "Exportación (Inglés)",
    icon: "🌍",
    text: "Dear sales team, please process the following B2B order for our London boutiques:\n- 120 units of 'Oxford Classic' men's shoes (SKU: OX-100), mixed sizes UK 8 to UK 11\n- 45 units of Leather Tote Bags (SKU: BAG-LT-Brown)\nBill to: UK Fashion Retailers Ltd.\nPlease apply our standard 15% wholesale discount and confirm ETA."
  },
  {
    mode: 'online',
    label: "Caso Borde",
    icon: "⚠️",
    text: "Necesito lo de siempre para la oficina. Mándalo rápido y ajusta precios como la última vez."
  },
  {
    mode: 'offline',
    label: "Offline",
    icon: "📵",
    text: "Hola equipo, necesitamos para esta semana:\n- 12 cajas de papel A4 a €14.50 cada una\n- 30 bolígrafos azules a €1.80 cada uno\n- 6 cuadernos tamaño carta a €4.25 cada uno\n- 3 engrapadoras a €9.90 cada una\nFacturar a nombre de ACME Industrial.\nPor favor confirmar disponibilidad y tiempo de entrega."
  }
];

/**
 * SmartOrder — Enterprise-grade order processor.
 * Optimized for both desktop dashboards and natural mobile scrolling.
 */
export default function SmartOrder() {
  const [rawText, setRawText] = useState('');
  const [useOfflineMode, setUseOfflineMode] = useState(false);
  const { result, isLoading, error, processOrder, processOrderOffline, reset } = useOrderProcessor();
  const hasBillingMarker = BILLING_MARKER_REGEX.test(rawText);

  const handleReset = () => {
    setRawText('');
    setUseOfflineMode(false);
    reset();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (useOfflineMode) {
      await processOrderOffline();
      return;
    }

    const inferredClientName = extractBillingClientName(rawText);
    await processOrder({
      rawText,
      ...DEFAULT_CLIENT,
      clientName: inferredClientName ?? DEFAULT_CLIENT.clientName,
    });
  };

  return (
    <div className="flex flex-col min-h-full md:h-full bg-gray-50 font-sans text-gray-900">
      <div className="flex-1 flex flex-col md:flex-row gap-6 p-4 md:p-6 md:min-h-0">

        {/* ── Left Column: Input ── */}
        <section className="flex-1 flex flex-col bg-white rounded-xl shadow-sm border border-gray-200 md:min-h-0 overflow-hidden">
          <div className="p-4 border-b border-gray-100 flex-shrink-0">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-[10px] font-bold tracking-wider text-blue-600 bg-blue-50 px-2 py-0.5 rounded border border-blue-100 uppercase">
                Entrada
              </span>
            </div>
            <h2 className="text-lg font-bold text-gray-800 leading-tight">Pedido No Estructurado</h2>
            <p className="text-sm text-gray-500">Pega un pedido B2B — correo, WhatsApp o texto libre.</p>
          </div>

          <form className="flex-1 flex flex-col md:min-h-0" onSubmit={handleSubmit}>
            <div className="px-4 pt-4 border-b border-gray-50 bg-gray-50/10 shrink-0">
              <span className="text-[10px] uppercase font-bold text-gray-400 tracking-wider block mb-2">
                Ejemplos rápidos:
              </span>
              <div className="flex flex-wrap gap-2 pb-3">
                {EXAMPLES.map((ex, idx) => (
                  <button
                    key={idx}
                    type="button"
                    onClick={() => {
                      setRawText(ex.text);
                      setUseOfflineMode(ex.mode === 'offline');
                    }}
                    className="inline-flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full bg-white border border-gray-200 text-gray-600 hover:bg-blue-50 hover:text-blue-600 hover:border-blue-200 transition-all shadow-sm whitespace-nowrap active:scale-95"
                  >
                    <span>{ex.icon}</span>
                    <span className="font-medium">{ex.label}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="flex-1 p-4 md:overflow-y-auto">
              <textarea
                className="w-full min-h-[300px] md:h-full p-4 text-sm leading-relaxed text-gray-800 bg-white border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all resize-none placeholder:italic placeholder:text-gray-400"
                value={rawText}
                onChange={(e) => setRawText(e.target.value)}
                placeholder={`Ejemplo:\n"Hola, necesitamos 3 cajas de papel A4, 10 bolígrafos y 2 grapadoras. Por favor, confirma disponibilidad."`}
                disabled={isLoading}
                aria-label="Texto del pedido"
              />
            </div>

            {/* Actions Footer (Sticky/Bottom) */}
            <div className="p-4 bg-gray-50 border-t border-gray-100 flex items-center justify-between flex-shrink-0 h-[73px]">
              <div className="flex gap-3">
                <button
                  type="submit"
                  className="inline-flex items-center justify-center px-6 py-2.5 text-sm font-semibold text-white bg-blue-600 rounded-lg shadow-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-blue-300 disabled:cursor-not-allowed transition-all"
                  disabled={isLoading || !rawText.trim()}
                >
                  {isLoading ? (
                    <>
                      <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                      </svg>
                      Procesando...
                    </>
                  ) : (
                    useOfflineMode ? '📵 Procesar en modo offline' : '⚡ Procesar con IA'
                  )}
                </button>

                {rawText && !isLoading && (
                  <button
                    type="button"
                    onClick={() => setRawText('')}
                    className="px-4 py-2.5 text-sm font-medium text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-all"
                  >
                    Limpiar
                  </button>
                )}
              </div>
            </div>
          </form>
        </section>

        {/* ── Right Column: Output ── */}
        <section className="flex-1 flex flex-col bg-white rounded-xl shadow-sm border border-gray-200 md:min-h-0 overflow-hidden">
          <div className="p-4 bg-white border-b border-gray-100 flex-shrink-0">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-[10px] font-bold tracking-wider text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100 uppercase">
                Salida
              </span>
            </div>
            <h2 className="text-lg font-bold text-gray-800 leading-tight">Pedido Estructurado</h2>
            <p className="text-sm text-gray-500">Resultado analizado por la IA desde Groq API.</p>
          </div>

          <div className="flex-1 p-4 md:overflow-y-auto bg-gray-50/30 min-h-[300px] md:min-h-0">
            {!result && !error && !isLoading && (
              <div className="h-full min-h-[300px] flex flex-col items-center justify-center text-gray-400 space-y-4">
                <svg className="w-16 h-16 opacity-20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
                </svg>
                <p className="text-sm">Tu pedido estructurado aparecerá aquí.</p>
              </div>
            )}

            {isLoading && (
              <div className="h-full min-h-[300px] flex flex-col items-center justify-center space-y-4">
                <div className="relative">
                  <div className="absolute inset-0 animate-ping rounded-full bg-blue-100 opacity-75" />
                  <svg className="relative w-16 h-16 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
                  </svg>
                </div>
                <p className="text-sm font-medium text-gray-600">Llamando a Groq AI...</p>
              </div>
            )}

            {error && (
              <div className="p-4 bg-red-50 border border-red-100 rounded-lg text-sm text-red-600">
                <span className="font-bold">Error:</span> {error}
              </div>
            )}

            {result && !isLoading && (
              <div className="space-y-4">
                {/* Visual Summary */}
                <div className="grid grid-cols-2 gap-3 p-3 bg-white border border-gray-100 rounded-lg shadow-sm">
                  <div>
                    <span className="block text-gray-400 uppercase font-bold text-[9px]">Pedido #</span>
                    <span className="font-mono text-sm text-gray-700">{result.orderId.slice(0, 8)}...</span>
                  </div>
                  <div className="text-right">
                    <span className="block text-gray-400 uppercase font-bold text-[9px]">Importe Total</span>
                    <span className="text-sm font-bold text-gray-900">{formatEur(result.totalAmount)}</span>
                  </div>
                  <div>
                    <span className="block text-gray-400 uppercase font-bold text-[9px]">Cliente</span>
                    <span className="text-sm text-gray-700 font-medium whitespace-nowrap overflow-hidden text-ellipsis">{result.clientName}</span>
                  </div>
                  <div className="text-right flex items-end justify-end">
                    <span className="px-2 py-0.5 rounded text-[9px] font-bold uppercase bg-emerald-100 text-emerald-700 border border-emerald-200">
                      {result.status}
                    </span>
                  </div>
                </div>

                {/* Professional JSON Code Block */}
                <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                  <div className="px-4 py-2 bg-gray-50 border-b border-gray-100 flex items-center justify-between">
                    <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">JSON Output</span>
                  </div>
                  <div className="p-1">
                    <SyntaxHighlighter
                      language="json"
                      style={{
                        ...github,
                        'hljs-attr': { color: '#1e40af', fontWeight: 'bold' },
                        'hljs-string': { color: '#059669' }, // Emerald-600 for strings
                        'hljs-number': { color: '#d97706' }, // Amber-600 for numbers
                      }}
                      customStyle={{
                        background: 'transparent',
                        padding: '1rem',
                        fontSize: '0.85rem',
                        lineHeight: '1.6',
                        margin: 0
                      }}
                      codeTagProps={{
                        style: {
                          fontFamily: 'inherit'
                        }
                      }}
                    >
                      {JSON.stringify(result, null, 2)}
                    </SyntaxHighlighter>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Sticky Actions Footer (Mirror) */}
          <div className="p-4 bg-gray-50 border-t border-gray-100 flex items-center justify-end flex-shrink-0 h-[73px]">
            {(result || error) && (
              <button
                type="button"
                onClick={handleReset}
                className="px-6 py-2.5 text-sm font-semibold text-gray-600 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 hover:text-gray-800 transition-all focus:outline-none focus:ring-2 focus:ring-blue-500 active:bg-gray-100"
              >
                Reiniciar
              </button>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
