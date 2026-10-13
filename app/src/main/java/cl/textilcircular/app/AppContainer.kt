package cl.textilcircular.app

import android.content.Context
import cl.textilcircular.app.data.local.AppDatabase
import cl.textilcircular.app.data.media.ImageStorage
import cl.textilcircular.app.data.repository.AuthRepository
import cl.textilcircular.app.data.repository.ChatRepository
import cl.textilcircular.app.data.repository.MarketplaceRepository
import cl.textilcircular.app.data.repository.SociaRepository
import cl.textilcircular.app.data.seed.DemoSeeder
import cl.textilcircular.app.data.session.SessionStore
import cl.textilcircular.app.platform.NotificationHelper

/** Inyección de dependencias manual: un único punto que arma la capa de datos. */
class AppContainer(context: Context) {
    private val db = AppDatabase.crear(context)

    val session = SessionStore(context)
    val imageStorage = ImageStorage(context)
    val notificationHelper = NotificationHelper(context)
    val seeder = DemoSeeder(db)

    val authRepository = AuthRepository(db.sociaDao(), db.publicacionDao(), session, imageStorage)
    val sociaRepository = SociaRepository(db.sociaDao(), db.rubroDao())
    val marketplaceRepository = MarketplaceRepository(db.publicacionDao(), db.rubroDao(), db.conversacionDao(), imageStorage)
    val chatRepository = ChatRepository(db.conversacionDao(), db.publicacionDao(), db.novedadDao(), session, notificationHelper)
}
