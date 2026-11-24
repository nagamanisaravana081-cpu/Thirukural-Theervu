const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, ts = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    displayName: "Ananya Sundaram",
    email: "ananya@example.com",
    displayLanguage: "BILINGUAL",
    voiceLanguage: "TAMIL_IN",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validSavedKuralPayload(uid, ts = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    query: "Feeling overwhelmed with work deadlines and stress",
    detectedSummaryEn: "You are feeling overwhelmed by mounting work pressure.",
    detectedSummaryTa: "அதிக வேலைப் பளுவால் மன அழுத்தம் உணர்கிறீர்கள்.",
    kuralNumber: 621,
    kuralLine1Ta: "இடுக்கண் வருங்கால் நகுக அதனை",
    kuralLine2Ta: "அடுத்தூர்வது அஃதொப்பது இல்.",
    athikaramNumber: 63,
    athikaramTa: "இடுக்கண் அழியாமை",
    athikaramEn: "Hopefulness in Trouble",
    transliteration: "Idukkan varungaal naguga adhanai / Aduththoorvadhu akdhoppadhu il",
    meaningTa: "துன்பம் வரும்போது மனம் கலங்காமல் சிரித்து எதிர்கொள்ள வேண்டும்.",
    meaningEn: "When adversity strikes, meet it with a calm smile; nothing overcomes hardship faster.",
    practicalSolutionTa: "இன்று உங்கள் வேலைகளைச் சிறிய பகுதிகளாகப் பிரித்து அமைதியாக அணுகுங்கள்.",
    practicalSolutionEn: "Step back, take a slow breath, and break your workload into one calm priority at a time.",
    actionableStepTa: "இப்போது 2 நிமிடம் ஆழ்ந்த மூச்சு எடுத்து மிக முக்கியமான ஒரு வேலையை மட்டும் தொடங்குங்கள்.",
    actionableStepEn: "Take 2 minutes to breathe deeply and write down just the single most important task for the next hour.",
    topicTag: "Stress",
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write profiles or savedKurals", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("savedKurals").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("savedKurals")
      .doc("kural_621")
      .set(validSavedKuralPayload(ALICE_UID))
  );
});

test("Authenticated owner can create, read, and partially update own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Partial update matching Kotlin repository updatePreferences()
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayLanguage: "TAMIL",
      voiceLanguage: "TANGLISH_EN_IN",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field / role escalation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject mutating immutable createdAt
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject invalid language enum
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayLanguage: "FRENCH",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject listing /users root collection
  await assertFails(aliceDb.collection("users").get());
});

test("Authenticated owner can create, list, update, and delete own savedKurals", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("savedKurals");

  // Empty subcollection list query (matches Kotlin observeSavedKurals)
  await assertSucceeds(col.orderBy("createdAt", "desc").get());

  // Create saved kural (NO @DocumentId `id` field)
  await assertSucceeds(
    col.doc("kural_621").set(validSavedKuralPayload(ALICE_UID, created))
  );

  // Read & list
  await assertSucceeds(col.doc("kural_621").get());
  await assertSucceeds(col.orderBy("createdAt", "desc").get());

  // Partial update preserving createdAt and kuralNumber
  await assertSucceeds(
    col.doc("kural_621").update({
      query: "Work pressure feels too heavy today",
      topicTag: "Stress",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject @DocumentId `id` field injection on create
  await assertFails(
    col.doc("kural_622").set({
      ...validSavedKuralPayload(ALICE_UID, created),
      id: "kural_622",
    })
  );

  // Reject out-of-range Kural number (> 1330)
  await assertFails(
    col.doc("kural_9999").set({
      ...validSavedKuralPayload(ALICE_UID, created),
      kuralNumber: 1331,
    })
  );

  // Reject future tampered timestamp
  await assertFails(
    col.doc("kural_623").set(
      validSavedKuralPayload(ALICE_UID, new Date(Date.now() + 60000))
    )
  );

  // Delete own saved kural
  await assertSucceeds(col.doc("kural_621").delete());
});

test("Cross-user isolation: Bob cannot read, list, create, update, or delete Alice's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID));
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("savedKurals")
      .doc("kural_621")
      .set(validSavedKuralPayload(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("savedKurals").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("savedKurals")
      .doc("kural_621")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("savedKurals")
      .doc("kural_621")
      .delete()
  );
  // Spoofed userId inside Bob's own path must also fail
  await assertFails(
    bobDb
      .collection("users")
      .doc(BOB_UID)
      .collection("savedKurals")
      .doc("kural_621")
      .set(validSavedKuralPayload(ALICE_UID))
  );
});
