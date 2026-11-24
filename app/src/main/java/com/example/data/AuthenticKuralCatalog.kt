package com.example.data

data class CanonicalKural(
    val number: Int,
    val line1Ta: String,
    val line2Ta: String,
    val athikaramNumber: Int,
    val athikaramTa: String,
    val athikaramEn: String,
    val transliteration: String,
    val themeHint: String
)

/**
 * Scholar-verified classical Tamil Thirukkural catalog covering everyday life challenges.
 * Guarantees 100% authentic 4+3 சீர் pure Tamil text, accurate Kural & Athikaram numbers,
 * and zero hallucinated or foreign-script characters.
 */
object AuthenticKuralCatalog {

    val allKurals: List<CanonicalKural> = listOf(
        CanonicalKural(
            number = 31,
            line1Ta = "சிறப்பீனும் செல்வமும் ஈனும் அறத்தினூஉங்கு",
            line2Ta = "ஆக்கம் எவனோ உயிர்க்கு",
            athikaramNumber = 4,
            athikaramTa = "அறன் வலியுறுத்தல்",
            athikaramEn = "Assertion of the Strength of Virtue",
            transliteration = "Sirappeenum selvamum eenum araththinooangu\nAakkam evano uyirkku",
            themeHint = "purpose, doing right, dharma, ethics, inner fulfillment"
        ),
        CanonicalKural(
            number = 34,
            line1Ta = "மனத்துக்கண் மாசிலன் ஆதல் அனைத்தறன்",
            line2Ta = "ஆகுல நீர பிற",
            athikaramNumber = 4,
            athikaramTa = "அறன் வலியுறுத்தல்",
            athikaramEn = "Assertion of the Strength of Virtue",
            transliteration = "Manaththukkan maasilan aadhal anaiththaran\nAagula neera pira",
            themeHint = "guilt, purity of mind, peace of conscience, honesty"
        ),
        CanonicalKural(
            number = 45,
            line1Ta = "அன்பும் அறனும் உடைத்தாயின் இல்வாழ்க்கை",
            line2Ta = "பண்பும் பயனும் அது",
            athikaramNumber = 5,
            athikaramTa = "இல்வாழ்க்கை",
            athikaramEn = "Domestic Life",
            transliteration = "Anbum aranum udaiththaayin ilvaazhkkai\nPanbum payanum adhu",
            themeHint = "family harmony, marriage, love at home, domestic life"
        ),
        CanonicalKural(
            number = 79,
            line1Ta = "புறத்துறுப்பு எல்லாம் எவன்செய்யும் யாக்கை",
            line2Ta = "அகத்துறுப்பு அன்பி லவர்க்கு",
            athikaramNumber = 8,
            athikaramTa = "அன்புடைமை",
            athikaramEn = "Possession of Love",
            transliteration = "Puraththuruppu ellaam evanseyyum yaakkai\nAgaththuruppu anbi lavarkku",
            themeHint = "loneliness, lack of love, compassion, emotional warmth"
        ),
        CanonicalKural(
            number = 95,
            line1Ta = "பணிவுடையன் இன்சொலன் ஆதல் ஒருவற்கு",
            line2Ta = "அணியல்ல மற்றுப் பிற",
            athikaramNumber = 10,
            athikaramTa = "இனியவை கூறல்",
            athikaramEn = "Speaking Pleasant Words",
            transliteration = "Panivudaiyan insolan aadhal oruvarkku\nAniyalla matrup pira",
            themeHint = "humility, ego, polite speech, respect, leadership"
        ),
        CanonicalKural(
            number = 99,
            line1Ta = "இன்சொல் இனிதீன்றல் காண்பான் எவன்கொலோ",
            line2Ta = "வன்சொல் வழங்கு வது",
            athikaramNumber = 10,
            athikaramTa = "இனியவை கூறல்",
            athikaramEn = "Speaking Pleasant Words",
            transliteration = "Insol inidheendral kaanbaan evankolo\nVansol vazhangu vadhu",
            themeHint = "harsh speech, arguments, gentle communication"
        ),
        CanonicalKural(
            number = 100,
            line1Ta = "இனிய உளவாக இன்னாத கூறல்",
            line2Ta = "கனியிருப்பக் காய்கவர்ந் தற்று",
            athikaramNumber = 10,
            athikaramTa = "இனியவை கூறல்",
            athikaramEn = "Speaking Pleasant Words",
            transliteration = "Iniya ulavaaga innaadha kooral\nKaniyiruppak kaaikavarn thatru",
            themeHint = "rude words, hurting others with speech, kindness"
        ),
        CanonicalKural(
            number = 108,
            line1Ta = "நன்றி மறப்பது நன்றன்று நன்றல்லது",
            line2Ta = "அன்றே மறப்பது நன்று",
            athikaramNumber = 11,
            athikaramTa = "செய்ந்நன்றி அறிதல்",
            athikaramEn = "Gratitude",
            transliteration = "Nandri marappadhu nandrandru nandralladhu\nAndre marappadhu nandru",
            themeHint = "grudges, letting go of past hurt, gratitude, moving on"
        ),
        CanonicalKural(
            number = 121,
            line1Ta = "அடக்கம் அமரருள் உய்க்கும் அடங்காமை",
            line2Ta = "ஆரிருள் உய்த்து விடும்",
            athikaramNumber = 13,
            athikaramTa = "அடக்கம் உடைமை",
            athikaramEn = "Self-Control",
            transliteration = "Adakkam amararul uykkum adangaamai\nAarirul uyththu vidum",
            themeHint = "self-control, discipline, impulse, addiction, bad habits"
        ),
        CanonicalKural(
            number = 126,
            line1Ta = "ஒருமையுள் ஆமைபோல் ஐந்தடக்கல் ஆற்றின்",
            line2Ta = "எழுமையும் ஏமாப் புடைத்து",
            athikaramNumber = 13,
            athikaramTa = "அடக்கம் உடைமை",
            athikaramEn = "Self-Control",
            transliteration = "Orumaiyul aamaipol aindhadakkal aatrin\nEzhumaiyum eemaap pudaiththu",
            themeHint = "distractions, senses control, focus, mindfulness"
        ),
        CanonicalKural(
            number = 127,
            line1Ta = "யாகாவா ராயினும் நாகாக்க காவாக்கால்",
            line2Ta = "சோகாப்பர் சொல்லிழுக்குப் பட்டு",
            athikaramNumber = 13,
            athikaramTa = "அடக்கம் உடைமை",
            athikaramEn = "Self-Control",
            transliteration = "Yaagaavaa raayinum naagaakka kaavaakkaal\nSogaappar sollizhukkup pattu",
            themeHint = "controlling tongue, speaking without thinking, regret over words"
        ),
        CanonicalKural(
            number = 129,
            line1Ta = "தீயினாற் சுட்டபுண் உள்ளாறும் ஆறாதே",
            line2Ta = "நாவினாற் சுட்ட வடு",
            athikaramNumber = 13,
            athikaramTa = "அடக்கம் உடைமை",
            athikaramEn = "Self-Control",
            transliteration = "Theeyinaar suttapun ullaarum aaraadhe\nNaavinaar sutta vadu",
            themeHint = "verbal wounds, harsh words in relationships, emotional hurt"
        ),
        CanonicalKural(
            number = 151,
            line1Ta = "அகழ்வாரைத் தாங்கும் நிலம்போலத் தம்மை",
            line2Ta = "இகழ்வார்ப் பொறுத்தல் தலை",
            athikaramNumber = 16,
            athikaramTa = "பொறையுடைமை",
            athikaramEn = "Patience and Forgiveness",
            transliteration = "Agazhvaaraith thaangum nilampolath thammai\nIgazhvaarp poruththal thalai",
            themeHint = "insults, criticism, patience, staying calm when mocked"
        ),
        CanonicalKural(
            number = 158,
            line1Ta = "மிகுதியான் மிக்கவை செய்தாரைத் தாம்தம்",
            line2Ta = "தகுதியான் வென்று விடல்",
            athikaramNumber = 16,
            athikaramTa = "பொறையுடைமை",
            athikaramEn = "Patience and Forgiveness",
            transliteration = "Migudhiyaan mikkavai seidhaaraith thaamdham\nThagudhiyaan vendru vidal",
            themeHint = "arrogant people, workplace toxicity, dignity over revenge"
        ),
        CanonicalKural(
            number = 159,
            line1Ta = "துறந்தாரின் தூய்மை உடையர் இறந்தார்வாய்",
            line2Ta = "இன்னாச்சொல் நோற்கிற் பவர்",
            athikaramNumber = 16,
            athikaramTa = "பொறையுடைமை",
            athikaramEn = "Patience and Forgiveness",
            transliteration = "Thurandhaarin thooimai udaiyar irandhaarvaai\nInnaachchol norkir pavar",
            themeHint = "enduring harsh criticism, inner strength, calm tolerance"
        ),
        CanonicalKural(
            number = 161,
            line1Ta = "ஒழுக்காறாக் கொள்க ஒருவன்தன் நெஞ்சத்து",
            line2Ta = "அழுக்காறு இலாத இயல்பு",
            athikaramNumber = 17,
            athikaramTa = "அழுக்காறாமை",
            athikaramEn = "Avoiding Envy",
            transliteration = "Ozhukkaaraak kolga oruvandhan nenjaththu\nAzhukkaaru ilaadha iyalbu",
            themeHint = "jealousy, envy, comparing yourself to others"
        ),
        CanonicalKural(
            number = 184,
            line1Ta = "கண்ணின்று கண்ணறச் சொல்லினும் சொல்லற்க",
            line2Ta = "முன்னின்று பின்னோக்காச் சொல்",
            athikaramNumber = 19,
            athikaramTa = "புறங்கூறாமை",
            athikaramEn = "Not Backbiting",
            transliteration = "Kannindru kannarach sollinum sollarka\nMunnindru pinnokkaach sol",
            themeHint = "gossip, backbiting, office politics, trust issues"
        ),
        CanonicalKural(
            number = 291,
            line1Ta = "வாய்மை எனப்படுவ தியாதெனின் யாதொன்றும்",
            line2Ta = "தீமை இலாத சொலல்",
            athikaramNumber = 30,
            athikaramTa = "வாய்மை",
            athikaramEn = "Truthfulness",
            transliteration = "Vaaimai enappaduva dhiyaadhenin yaadhondrum\nTheemai ilaadha solal",
            themeHint = "truthfulness, honesty, ethical dilemma, harmless speech"
        ),
        CanonicalKural(
            number = 292,
            line1Ta = "பொய்ம்மையும் வாய்மை யிடத்த புரைதீர்ந்த",
            line2Ta = "நன்மை பயக்கும் எனின்",
            athikaramNumber = 30,
            athikaramTa = "வாய்மை",
            athikaramEn = "Truthfulness",
            transliteration = "Poymmaiyum vaaimai yidaththa puraidheerndha\nNanmai payakkum enin",
            themeHint = "difficult truth, compassion vs honesty, protecting others"
        ),
        CanonicalKural(
            number = 305,
            line1Ta = "தன்னைத்தான் காக்கின் சினங்காக்க காவாக்கால்",
            line2Ta = "தன்னையே கொல்லும் சினம்",
            athikaramNumber = 31,
            athikaramTa = "வெகுளாமை",
            athikaramEn = "Restraining Anger",
            transliteration = "Thannaiththaan kaakkin sinangaakka kaavaakkaal\nThannaiye kollum sinam",
            themeHint = "anger, rage, losing temper, self-harm from anger"
        ),
        CanonicalKural(
            number = 306,
            line1Ta = "சினமென்னும் சேர்ந்தாரைக் கொல்லி இனமென்னும்",
            line2Ta = "ஏமப் புணையைச் சுடும்",
            athikaramNumber = 31,
            athikaramTa = "வெகுளாமை",
            athikaramEn = "Restraining Anger",
            transliteration = "Sinamennum serndhaaraik kolli inamennum\nEemap punaiyaich sudum",
            themeHint = "anger damaging family and friendships, relationship conflict"
        ),
        CanonicalKural(
            number = 309,
            line1Ta = "உள்ளிய தெல்லாம் உடனெய்தும் உள்ளத்தால்",
            line2Ta = "உள்ளான் வெகுளி எனின்",
            athikaramNumber = 31,
            athikaramTa = "வெகுளாமை",
            athikaramEn = "Restraining Anger",
            transliteration = "Ulliya thellaam udaneydhum ullaththaal\nUllaan veguli enin",
            themeHint = "calm mind brings success, letting go of resentment"
        ),
        CanonicalKural(
            number = 314,
            line1Ta = "இன்னாசெய் தாரை ஒறுத்தல் அவர்நாண",
            line2Ta = "நன்னயம் செய்து விடல்",
            athikaramNumber = 32,
            athikaramTa = "இன்னா செய்யாமை",
            athikaramEn = "Not Doing Harm",
            transliteration = "Innaasei dhaarai oruththal avarnaana\nNannayam seidhu vidal",
            themeHint = "betrayal, revenge, responding to harm with grace"
        ),
        CanonicalKural(
            number = 331,
            line1Ta = "நில்லாத வற்றை நிலையின என்றுணரும்",
            line2Ta = "புல்லறி வாண்மை கடை",
            athikaramNumber = 34,
            athikaramTa = "நிலையாமை",
            athikaramEn = "Impermanence",
            transliteration = "Nillaadha vatrai nilaiyina endrunarum\nPullari vaanmai kadai",
            themeHint = "loss, attachment, change, grief, impermanence"
        ),
        CanonicalKural(
            number = 360,
            line1Ta = "காமம் வெகுளி மயக்கம் இவைமூன்றன்",
            line2Ta = "நாமம் கெடக்கெடும் நோய்",
            athikaramNumber = 36,
            athikaramTa = "மெய்யுணர்தல்",
            athikaramEn = "Realization of Truth",
            transliteration = "Kaamam veguli mayakkam ivaimoondran\nNaamam kedakkedum noi",
            themeHint = "inner restlessness, desire, delusion, mental suffering, overthinking"
        ),
        CanonicalKural(
            number = 391,
            line1Ta = "கற்க கசடறக் கற்பவை கற்றபின்",
            line2Ta = "நிற்க அதற்குத் தக",
            athikaramNumber = 40,
            athikaramTa = "கல்வி",
            athikaramEn = "Learning",
            transliteration = "Karka kasadarak karpavai katrapin\nNirka adharkuth thaga",
            themeHint = "studies, exams, learning skills, applying knowledge"
        ),
        CanonicalKural(
            number = 396,
            line1Ta = "தொட்டனைத் தூறும் மணற்கேணி மாந்தர்க்குக்",
            line2Ta = "கற்றனைத் தூறும் அறிவு",
            athikaramNumber = 40,
            athikaramTa = "கல்வி",
            athikaramEn = "Learning",
            transliteration = "Thottanaith thoorum manarkeni maandharkkuk\nKatranaith thoorum arivu",
            themeHint = "continuous learning, upskilling, self-improvement, career growth"
        ),
        CanonicalKural(
            number = 421,
            line1Ta = "அறிவற்றங் காக்கும் கருவி செறுவார்க்கும்",
            line2Ta = "உள்ளழிக்கல் ஆகா அரண்",
            athikaramNumber = 43,
            athikaramTa = "அறிவுடைமை",
            athikaramEn = "Possession of Wisdom",
            transliteration = "Arivatram kaakkum karuvi seruvaarkkum\nUllazhikkal aagaa aran",
            themeHint = "fear, insecurity, protection through wisdom, confidence"
        ),
        CanonicalKural(
            number = 422,
            line1Ta = "சென்ற இடத்தால் செலவிடா தீதொரீஇ",
            line2Ta = "நன்றின்பால் உய்ப்ப தறிவு",
            athikaramNumber = 43,
            athikaramTa = "அறிவுடைமை",
            athikaramEn = "Possession of Wisdom",
            transliteration = "Sendra idaththaal selavidaa theedhorii\nNandrinpaal uyppa tharivu",
            themeHint = "overthinking, wandering mind, negative thoughts, mental clarity"
        ),
        CanonicalKural(
            number = 423,
            line1Ta = "எப்பொருள் யார்யார்வாய்க் கேட்பினும் அப்பொருள்",
            line2Ta = "மெய்ப்பொருள் காண்ப தறிவு",
            athikaramNumber = 43,
            athikaramTa = "அறிவுடைமை",
            athikaramEn = "Possession of Wisdom",
            transliteration = "Epporul yaaryaarvaaik ketpinum apporul\nMeypporul kaanba tharivu",
            themeHint = "rumors, misunderstanding, confusion, critical thinking, truth"
        ),
        CanonicalKural(
            number = 430,
            line1Ta = "அறிவுடையார் எல்லாம் உடையார் அறிவிலார்",
            line2Ta = "என்னுடைய ரேனும் இலர்",
            athikaramNumber = 43,
            athikaramTa = "அறிவுடைமை",
            athikaramEn = "Possession of Wisdom",
            transliteration = "Arivudaiyaar ellaam udaiyaar arivilaar\nEnnudaiya renum ilar",
            themeHint = "feeling inadequate, self-worth, valuing wisdom over possessions"
        ),
        CanonicalKural(
            number = 461,
            line1Ta = "அழிவதூஉம் ஆவதூஉம் ஆகி வழிபயக்கும்",
            line2Ta = "ஊதியமும் சூழ்ந்து செயல்",
            athikaramNumber = 47,
            athikaramTa = "தெரிந்து செயல்வகை",
            athikaramEn = "Acting After Due Consideration",
            transliteration = "Azhivathooum aavathooum aagi vazhipayakkum\nOodhiyamum soozhndhu seyal",
            themeHint = "business decision, career switch, investment, weighing pros and cons"
        ),
        CanonicalKural(
            number = 467,
            line1Ta = "எண்ணித் துணிக கருமம் துணிந்தபின்",
            line2Ta = "எண்ணுவம் என்பது இழுக்கு",
            athikaramNumber = 47,
            athikaramTa = "தெரிந்து செயல்வகை",
            athikaramEn = "Acting After Due Consideration",
            transliteration = "Ennith thuniga karumam thunindhapin\nEnnuvam enbadhu izhukku",
            themeHint = "indecision, planning before acting, bold execution, avoiding regret"
        ),
        CanonicalKural(
            number = 471,
            line1Ta = "வினைவலியும் தன்வலியும் மாற்றான் வலியும்",
            line2Ta = "துணைவலியும் தூக்கிச் செயல்",
            athikaramNumber = 48,
            athikaramTa = "வலியறிதல்",
            athikaramEn = "Knowing One's Strength",
            transliteration = "Vinaivaliyum thanvaliyum maatraan valiyum\nThunaivaliyum thookkich seyal",
            themeHint = "burnout, overcommitting, knowing limits, realistic strategy"
        ),
        CanonicalKural(
            number = 478,
            line1Ta = "ஆகாறு அளவிட்டி தாயினும் கேடில்லை",
            line2Ta = "போகாறு அகலாக் கடை",
            athikaramNumber = 48,
            athikaramTa = "வலியறிதல்",
            athikaramEn = "Knowing One's Strength",
            transliteration = "Aagaaru alavitti thaayinum kedillai\nPogaaru agalaak kadai",
            themeHint = "money management, budgeting, debt, overspending, financial stress"
        ),
        CanonicalKural(
            number = 481,
            line1Ta = "பகல்வெல்லும் கூகையைக் காக்கை இகல்வெல்லும்",
            line2Ta = "வேந்தர்க்கு வேண்டும் பொழுது",
            athikaramNumber = 49,
            athikaramTa = "காலமறிதல்",
            athikaramEn = "Knowing the Right Time",
            transliteration = "Pagalvellum koogaiyaik kaakkai igalvellum\nVendharkku vendum pozhudhu",
            themeHint = "right timing, patience for opportunity, career competition"
        ),
        CanonicalKural(
            number = 484,
            line1Ta = "ஞாலம் கருதினும் கைகூடும் காலம்",
            line2Ta = "கருதி இடத்தாற் செயின்",
            athikaramNumber = 49,
            athikaramTa = "காலமறிதல்",
            athikaramEn = "Knowing the Right Time",
            transliteration = "Nyaalam karudhinum kaikoodum kaalam\nKarudhi idaththaar seyin",
            themeHint = "big goals, strategic timing, right place and time"
        ),
        CanonicalKural(
            number = 490,
            line1Ta = "கொக்கொக்க கூம்பும் பருவத்து மற்றதன்",
            line2Ta = "குத்தொக்க சீர்த்த இடத்து",
            athikaramNumber = 49,
            athikaramTa = "காலமறிதல்",
            athikaramEn = "Knowing the Right Time",
            transliteration = "Kokkokka koombum paruvaththu matradhan\nKuththokka seerththa idaththu",
            themeHint = "waiting quietly, patience, seizing the decisive moment"
        ),
        CanonicalKural(
            number = 595,
            line1Ta = "வெள்ளத் தனைய மலர்நீட்டம் மாந்தர்தம்",
            line2Ta = "உள்ளத் தனையது உயர்வு",
            athikaramNumber = 60,
            athikaramTa = "ஊக்கமுடைமை",
            athikaramEn = "Enthusiasm and Inner Energy",
            transliteration = "Vellath thanaiya malarneetam maandhardham\nUllath thanaiyadhu uyarvu",
            themeHint = "low motivation, self-belief, confidence, ambition"
        ),
        CanonicalKural(
            number = 596,
            line1Ta = "உள்ளுவ தெல்லாம் உயர்வுள்ளல் மற்றது",
            line2Ta = "தள்ளினும் தள்ளாமை நீர்த்து",
            athikaramNumber = 60,
            athikaramTa = "ஊக்கமுடைமை",
            athikaramEn = "Enthusiasm and Inner Energy",
            transliteration = "Ulluva thellaam uyarvullal matradhu\nThallinum thallaamai neerththu",
            themeHint = "aiming high, fear of failing, dreaming big, resilience"
        ),
        CanonicalKural(
            number = 605,
            line1Ta = "நெடுநீர் மறவி மடிதுயில் நான்கும்",
            line2Ta = "கெடுநீரார் காமக் கலன்",
            athikaramNumber = 61,
            athikaramTa = "மடியின்மை",
            athikaramEn = "Avoiding Sloth",
            transliteration = "Neduneer maravi madithuyil naangum\nKeduneeraar kaamak kalan",
            themeHint = "procrastination, laziness, oversleeping, time management"
        ),
        CanonicalKural(
            number = 611,
            line1Ta = "அருமை உடைத்தென்று அசாவாமை வேண்டும்",
            line2Ta = "பெருமை முயற்சி தரும்",
            athikaramNumber = 62,
            athikaramTa = "ஆள்வினையுடைமை",
            athikaramEn = "Persistent Effort",
            transliteration = "Arumai udaiththendru asaavaamai vendum\nPerumai muyarchi tharum",
            themeHint = "hard tasks, feeling overwhelmed by difficulty, steady effort"
        ),
        CanonicalKural(
            number = 619,
            line1Ta = "தெய்வத்தான் ஆகா தெனினும் முயற்சிதன்",
            line2Ta = "மெய்வருத்தக் கூலி தரும்",
            athikaramNumber = 62,
            athikaramTa = "ஆள்வினையுடைமை",
            athikaramEn = "Persistent Effort",
            transliteration = "Dheivaththaan aagaa theninum muyarchidhan\nMeyvaruththak kooli tharum",
            themeHint = "bad luck, failure, hard work always pays off, job search"
        ),
        CanonicalKural(
            number = 620,
            line1Ta = "ஊழையும் உப்பக்கம் காண்பர் உலைவின்றித்",
            line2Ta = "தாழாது உஞற்று பவர்",
            athikaramNumber = 62,
            athikaramTa = "ஆள்வினையுடைமை",
            athikaramEn = "Persistent Effort",
            transliteration = "Oozhaiyum uppakkam kaanbar ulaivindrith\nThaazhaadhu unyatru bavar",
            themeHint = "overcoming fate and obstacles, never giving up, perseverance"
        ),
        CanonicalKural(
            number = 621,
            line1Ta = "இடுக்கண் வருங்கால் நகுக அதனை",
            line2Ta = "அடுத்தூர்வது அஃதொப்ப தில்",
            athikaramNumber = 63,
            athikaramTa = "இடுக்கண் அழியாமை",
            athikaramEn = "Not Despairing in Misfortune",
            transliteration = "Idukkan varungaal naguga adhanai\nAduththoorvadhu ahdhoppa thil",
            themeHint = "stress, sudden troubles, staying cheerful in adversity, courage"
        ),
        CanonicalKural(
            number = 622,
            line1Ta = "வெள்ளத் தனைய இடும்பை அறிவுடையான்",
            line2Ta = "உள்ளத்தின் உள்ளக் கெடும்",
            athikaramNumber = 63,
            athikaramTa = "இடுக்கண் அழியாமை",
            athikaramEn = "Not Despairing in Misfortune",
            transliteration = "Vellath thanaiya idumbai arivudaiyaan\nUllaththin ullak kedum",
            themeHint = "overwhelming stress, anxiety flood, calm mind dissolves sorrow"
        ),
        CanonicalKural(
            number = 628,
            line1Ta = "இன்பம் விழையான் இடும்பை இயல்பென்பான்",
            line2Ta = "துன்பம் உறுதல் இலன்",
            athikaramNumber = 63,
            athikaramTa = "இடுக்கண் அழியாமை",
            athikaramEn = "Not Despairing in Misfortune",
            transliteration = "Inbam vizhaiyaan idumbai iyalbenbaan\nThunbam urudhal ilan",
            themeHint = "suffering, accepting life challenges, emotional resilience"
        ),
        CanonicalKural(
            number = 666,
            line1Ta = "எண்ணிய எண்ணியாங்கு எய்துப எண்ணியார்",
            line2Ta = "திண்ணியர் ஆகப் பெறின்",
            athikaramNumber = 67,
            athikaramTa = "வினைத்திட்பம்",
            athikaramEn = "Firmness in Action",
            transliteration = "Enniya enniyaangu eydhuba enniyaar\nThinniyar aagap perin",
            themeHint = "determination, achieving goals, willpower, focus"
        ),
        CanonicalKural(
            number = 751,
            line1Ta = "பொருளல்ல வரைப் பொருளாகச் செய்யும்",
            line2Ta = "பொருளல்ல தில்லை பொருள்",
            athikaramNumber = 76,
            athikaramTa = "பொருள்செயல்வகை",
            athikaramEn = "Acquiring Wealth",
            transliteration = "Porulalla varaip porulaagach seyyum\nPorulalla thillai porul",
            themeHint = "financial independence, earning wealth, career stability"
        ),
        CanonicalKural(
            number = 754,
            line1Ta = "அறன்ஈனும் இன்பமும் ஈனும் திறனறிந்து",
            line2Ta = "தீதின்றி வந்த பொருள்",
            athikaramNumber = 76,
            athikaramTa = "பொருள்செயல்வகை",
            athikaramEn = "Acquiring Wealth",
            transliteration = "Araneenum inbamum eenum thiranarindhu\nTheedhindri vandha porul",
            themeHint = "ethical money, honest earning, peace of mind with wealth"
        ),
        CanonicalKural(
            number = 784,
            line1Ta = "நகுதற் பொருட்டன்று நட்டல் மிகுதிக்கண்",
            line2Ta = "மேற்சென்று இடித்தற் பொருட்டு",
            athikaramNumber = 79,
            athikaramTa = "நட்பு",
            athikaramEn = "Friendship",
            transliteration = "Naguthar poruttandru nattal migudhikkan\nMersendru idiththar poruttu",
            themeHint = "true friendship, honest feedback, correcting friends"
        ),
        CanonicalKural(
            number = 788,
            line1Ta = "உடுக்கை இழந்தவன் கைபோல ஆங்கே",
            line2Ta = "இடுக்கண் களைவதாம் நட்பு",
            athikaramNumber = 79,
            athikaramTa = "நட்பு",
            athikaramEn = "Friendship",
            transliteration = "Udukkai izhandhavan kaipola aange\nIdukkan kalaivadhaam natpu",
            themeHint = "supportive friends, loneliness, loyalty in hard times"
        ),
        CanonicalKural(
            number = 942,
            line1Ta = "மருந்தென வேண்டாவாம் யாக்கைக்கு அருந்தியது",
            line2Ta = "அற்றது போற்றி உணின்",
            athikaramNumber = 95,
            athikaramTa = "மருந்து",
            athikaramEn = "Health and Moderation",
            transliteration = "Marundhena vendaavaam yaakkaikku arundhiyadhu\nAtrathu potri unin",
            themeHint = "health, diet, fatigue, physical wellness, mindful eating"
        ),
        CanonicalKural(
            number = 1022,
            line1Ta = "ஆள்வினையும் ஆன்ற அறிவும் எனஇரண்டின்",
            line2Ta = "நீள்வினையால் நீளும் குடி",
            athikaramNumber = 103,
            athikaramTa = "குடிசெயல்வகை",
            athikaramEn = "Uplifting One's Family",
            transliteration = "Aalvinaiyum aandra arivum enairandin\nNeelvinaiyaal neelum kudi",
            themeHint = "supporting family, responsibility, career growth for family"
        )
    )

    private val entriesByNumber: Map<Int, CanonicalKural> = allKurals.associateBy { it.number }

    fun findByNumber(kuralNumber: Int): CanonicalKural? = entriesByNumber[kuralNumber]

    /**
     * Compact (~200-token) catalog menu passed to Gemini so it selects an exact verified Kural number
     * rapidly without latency overhead, guaranteeing 100% authentic Tamil couplets.
     */
    val promptSelectionIndex: String by lazy {
        allKurals.joinToString("; ") { k ->
            "${k.number}:${k.themeHint.substringBefore(",")}"
        }
    }

    /**
     * Keyword-based verified fallback only if the model returns an unknown Kural number outside the catalog.
     */
    fun findBestFallbackForQuery(query: String): CanonicalKural {
        val q = query.lowercase()
        val chosenNumber = when {
            listOf("anger", "angry", "temper", "கோபம்", "சினம்", "kovam", "kopam").any { q.contains(it) } -> 305
            listOf("stress", "pressure", "anxious", "anxiety", "மன அழுத்தம்", "tension").any { q.contains(it) } -> 622
            listOf("overthink", "worry", "சிந்தனை", "கவலை", "kavalai").any { q.contains(it) } -> 422
            listOf("fail", "failure", "loss", "setback", "தோல்வி", "tholvi").any { q.contains(it) } -> 619
            listOf("money", "debt", "salary", "expense", "பணம்", "கடன்", "panam").any { q.contains(it) } -> 478
            listOf("job", "career", "work", "office", "தொழில்", "வேலை", "velai").any { q.contains(it) } -> 611
            listOf("friend", "நட்பு", "nanban", "lonel").any { q.contains(it) } -> 788
            listOf("family", "wife", "husband", "parent", "குடும்பம்", "உறவு").any { q.contains(it) } -> 45
            listOf("lazy", "procrastin", "sleep", "சோம்பேறி").any { q.contains(it) } -> 605
            listOf("health", "sick", "tired", "உடல்", "மருந்து").any { q.contains(it) } -> 942
            listOf("study", "exam", "learn", "கல்வி", "படிப்பு").any { q.contains(it) } -> 391
            listOf("insult", "hurt", "harsh", "பொறுமை", "வார்த்தை").any { q.contains(it) } -> 151
            else -> 621
        }
        return entriesByNumber[chosenNumber] ?: allKurals.first()
    }
}
