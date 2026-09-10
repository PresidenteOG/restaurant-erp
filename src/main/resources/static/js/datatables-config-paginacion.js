/**
 * Configuración Global de DataTables - Restaurantus
 */
$(document).ready(function () {
    // 1. Buscamos todas las tablas que tengan la clase 'data-table'
    $('.data-table').each(function () {
        var tableId = $(this).attr('id');
        
        if (!tableId) return; // Si la tabla no tiene ID, saltamos
        var traduccionPrueba = /*[[#{manageUsers.tab_users}]]*/ 'Mostrar';

        // 2. Inicializamos DataTables con la configuración común
        var table = $('#' + tableId).DataTable({
            "language": window.datatableTranslations,
            "autoWidth": false,
            "orderCellsTop": true,
            "fixedHeader": true,
            "pageLength": 7,
            "lengthMenu": [7, 15, 25, 50, 100],
            // NO USAR, SE GESTIONA EN CADA PAGINA:
            // "language": {
            //     "url": "//cdn.datatables.net/plug-ins/1.13.6/i18n/es-ES.json",
            //     "emptyTable": "No hay datos disponibles en la tabla" 
            // },
            "dom": '<"d-flex justify-content-between mb-3"l>rt<"d-flex justify-content-between mt-3"ip>'
        });

        // 3. Activamos los buscadores de la fila '.filter-row' automáticamente
        $('#' + tableId + ' thead .filter-row input').on('keyup change', function () {
            var index = $(this).parent().index();
            if (table.column(index).search() !== this.value) {
                table.column(index).search(this.value).draw();
            }
        });

        //3. Para los que usan select en lugar de input
        $('#' + tableId + ' thead .filter-row select').on('keyup change', function () {
            var index = $(this).parent().index();
            if (table.column(index).search() !== this.value) {
                table.column(index).search(this.value).draw();
            }
        });
        
        // Evitamos que el clic en el input active el ordenamiento de la columna
        $('#' + tableId + ' thead .filter-row input').on('click', function(e) {
            e.stopPropagation();
        });
    });
});