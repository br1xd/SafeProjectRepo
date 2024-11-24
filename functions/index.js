const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();
const db = admin.firestore();

// Cloud Function para eliminar reportes vencidos
exports.eliminarReportesVencidos = functions.pubsub
    .schedule('every 1 minutes') // Esto sigue funcionando, pero con la nueva API de v6.x
    .timeZone('America/Santiago')  // Zona horaria de Chile (UTC-3)
    .onRun(async (context) => {
        try {
            const ahora = new Date();
            
            const snapshot = await db.collection('report-collection').get();
            const batch = db.batch();
            let contadorEliminados = 0;
            
            snapshot.docs.forEach(doc => {
                const data = doc.data();
                
                // Verificar que el documento tiene los campos necesarios
                if (data.fecha && data.tiempoDeVida !== undefined) {
                    const fechaReporte = data.fecha.toDate(); // Convertir Timestamp a Date
                    const tiempoDeVidaMs = data.tiempoDeVida  * 60 * 1000; // Convertir horas a milisegundos
                    const fechaExpiracion = new Date(fechaReporte.getTime() + tiempoDeVidaMs);
                    
                    if (fechaExpiracion < ahora) {
                        batch.delete(doc.ref);
                        contadorEliminados++;
                        console.log(`Reporte eliminado - ID: ${doc.id}, Tipo: ${data.tipo}, Autor: ${data.autor}`);
                    }
                }
            });
            
            if (contadorEliminados > 0) {
                await batch.commit();
                console.log(`${contadorEliminados} reportes vencidos eliminados exitosamente.`);
            } else {
                console.log('No se encontraron reportes vencidos para eliminar.');
            }
            
            return null;
        } catch (error) {
            console.error('Error al eliminar reportes:', error);
            console.error('Detalles del error:', error.message);
            throw new Error('Error al procesar la eliminación de reportes');
        }
    });
