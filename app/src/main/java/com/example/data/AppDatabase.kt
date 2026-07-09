package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Client::class,
        LawCase::class,
        DailyTask::class,
        LegalDraft::class,
        LawBook::class,
        CaseLaw::class,
        FeeRecord::class,
        LegalContact::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "legal_chamber_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.appDao())
                }
            }
        }

        suspend fun populateDatabase(dao: AppDao) {
            // --- Pre-populate Laws of Bangladesh ---
            dao.insertLawSection(
                LawBook(
                    name = "দণ্ডবিধি, ১৮৬০ (Penal Code)",
                    section = "ধারা ৪২০ (Section 420)",
                    title = "প্রতারণা ও সম্পত্তি অর্পণ করতে অসাধুভাবে প্ররোচিত করা",
                    description = "যদি কোনো ব্যক্তি প্রতারণা করে এবং তার ফলে অসাধুভাবে কোনো ব্যক্তিকে কোনো সম্পত্তি দিতে প্ররোচিত করে, তবে সে এই ধারায় অপরাধী হবে। এটি একটি জামিন অযোগ্য অপরাধ।",
                    punishment = "সর্বোচ্চ ৭ বছরের সশ্রম বা বিনাশ্রম কারাদণ্ড এবং অর্থদণ্ড।",
                    caseNotes = "প্রতারণার উদ্দেশ্যে প্রাথমিক অসৎ অভিপ্রায় (Initial dishonest intention) প্রমাণ করা অপরিহার্য। [৫০ ডিএলআর (এডি) ৮৩]"
                )
            )
            dao.insertLawSection(
                LawBook(
                    name = "দণ্ডবিধি, ১৮৬০ (Penal Code)",
                    section = "ধারা ৪০৬ (Section 406)",
                    title = "অপরাধমূলক বিশ্বাসভঙ্গের শাস্তি",
                    description = "কোনো ব্যক্তি যদি বিশ্বাসভঙ্গ করে কোনো গচ্ছিত বা অর্পিত সম্পত্তি আত্মসাৎ করে, তবে সে এই ধারায় অপরাধী হবে।",
                    punishment = "সর্বোচ্চ ৩ বছরের কারাদণ্ড, অর্থদণ্ড বা উভয় দণ্ড।",
                    caseNotes = "ব্যবসায়িক চুক্তির ক্ষেত্রে কেবল দেওয়ানি বিরোধ বলে উড়িয়ে দেওয়া যাবে না যদি ফৌজদারি বিশ্বাসভঙ্গ স্পষ্ট হয়।"
                )
            )
            dao.insertLawSection(
                LawBook(
                    name = "দেওয়ানি কার্যবিধি, ১৯০৮ (CPC)",
                    section = "আদেশ ৩৯ (Order 39)",
                    title = "অস্থায়ী নিষেধাজ্ঞা এবং অন্তর্বর্তীকালীন আদেশ",
                    description = "যে কোনো মামলায় যদি আশঙ্কা থাকে যে বিরোধীয় সম্পত্তি নষ্ট, ক্ষতিগ্রস্ত বা হস্তান্তরিত হতে পারে, তবে আদালত অস্থায়ী নিষেধাজ্ঞা জারির আদেশ দিতে পারেন।",
                    punishment = "আদেশ অমান্য করলে দেওয়ানি জেলে আটক বা সম্পত্তি ক্রোক করা যাবে।",
                    caseNotes = "নিষেধাজ্ঞা মঞ্জুর করার ৩টি প্রধান শর্ত: আপাতদৃষ্টে গ্রহণযোগ্য মামলা (Prima facie case), অপূরণীয় ক্ষতি (Irreparable loss) এবং সুবিধা-অসুবিধার ভারসাম্য (Balance of convenience)।"
                )
            )
            dao.insertLawSection(
                LawBook(
                    name = "সুনির্দিষ্ট প্রতিকার আইন, ১৮৭৭ (SR Act)",
                    section = "ধারা ৪২ (Section 42)",
                    title = "স্বত্ব বা পদমর্যাদার ঘোষণামূলক ডিক্রি",
                    description = "যেকোনো ব্যক্তি যার কোনো সম্পত্তিতে আইনগত অধিকার বা চরিত্র রয়েছে, তিনি আদালতের নিকট তার অধিকার ঘোষণার জন্য মামলা দায়ের করতে পারেন।",
                    punishment = "আদালতের স্বেচ্ছাধীন ক্ষমতা অনুযায়ী ডিক্রি প্রদান করা হবে।",
                    caseNotes = "মামলায় কেবল ঘোষণার সাথে যদি আনুষঙ্গিক প্রতিকার (Consequential Relief) যেমন দখল পুনরুদ্ধার দাবি করার সুযোগ থাকা সত্ত্বেও তা না চাওয়া হয়, তবে আদালত ঘোষণা দেবেন না।"
                )
            )
            dao.insertLawSection(
                LawBook(
                    name = "সাক্ষ্য আইন, ১৮৭২ (Evidence Act)",
                    section = "ধারা ৩০ (Section 30)",
                    title = "সহ-অভিযুক্তের স্বীকারোক্তি বিবেচনা",
                    description = "যখন যৌথভাবে বিচার চলাকালে কোনো এক জন অভিযুক্ত ব্যক্তি নিজের এবং অন্য সহ-অভিযুক্তদের জড়িয়ে কোনো স্বীকারোক্তি প্রদান করে, তখন আদালত তা সকলের বিরুদ্ধে বিবেচনা করতে পারেন।",
                    punishment = "সাক্ষ্য হিসেবে গ্রহণযোগ্য কিন্তু এককভাবে দণ্ড দেওয়ার জন্য পর্যাপ্ত নয়।",
                    caseNotes = "সহ-অভিযুক্তের স্বীকারোক্তি অত্যন্ত দুর্বল প্রকৃতির সাক্ষ্য এবং অন্য পারিপার্শ্বিক সাক্ষ্য দ্বারা সমর্থিত হতে হবে।"
                )
            )

            // --- Pre-populate Legal Draft Library ---
            dao.insertDraft(
                LegalDraft(
                    title = "জামিন আবেদন (Bail Petition - Section 497 CrPC)",
                    category = "Bail Petition",
                    templateContent = """
                        IN THE COURT OF THE METROPOLITAN MAGISTRATE, DHAKA.
                        CRIMINAL MISC. CASE NO. ________ OF 2026.
                        Arising out of G.R. Case No. ________ of 2026, corresponding to Ramna P.S. Case No. ____ dated ________ under Section 420/406 of the Penal Code.
                        In the matter of:
                        [আসামির নাম] ... Accused-Petitioner (In Jail)
                        -VERSUS-
                        The State ... Opposite Party
                        An application for bail under section 497 of the Code of Criminal Procedure.
                        The Petitioner most respectfully states as follows:
                        1. That the Petitioner is completely innocent and has been falsely implicated in this case out of local rivalry and malice.
                        2. That the allegations made...
                    """.trimIndent()
                )
            )
        }
    }
}

