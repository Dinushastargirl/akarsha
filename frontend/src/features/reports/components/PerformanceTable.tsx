import { useTranslation } from 'react-i18next';

interface Column<T> {
  header: string;
  accessor: (item: T) => React.ReactNode;
}

interface PerformanceTableProps<T> {
  title: string;
  data: T[];
  columns: Column<T>[];
  emptyMessage?: string;
}

export function PerformanceTable<T>({ title, data, columns, emptyMessage }: PerformanceTableProps<T>) {
  const { t } = useTranslation();

  return (
    <div className="bg-white rounded-lg border border-neutral-100 shadow-sm-flat overflow-hidden">
      <div className="px-6 py-4 border-b border-neutral-100 bg-neutral-50/50">
        <h3 className="font-serif text-lg text-neutral-900">{title}</h3>
      </div>
      
      {data.length === 0 ? (
        <div className="p-8 text-center text-neutral-400 text-sm">
          {emptyMessage || t('noDataAvailable')}
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead className="bg-neutral-50 text-neutral-500 font-medium">
              <tr>
                {columns.map((col, idx) => (
                  <th key={idx} className="px-6 py-3 border-b border-neutral-100">
                    {col.header}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-neutral-100">
              {data.map((item, idx) => (
                <tr key={idx} className="hover:bg-neutral-50/50 transition-colors">
                  {columns.map((col, colIdx) => (
                    <td key={colIdx} className="px-6 py-4 text-neutral-700">
                      {col.accessor(item)}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
