package com.taskmanager.seeder;

import com.taskmanager.entity.ExamPath;
import com.taskmanager.entity.ExamPhase;
import com.taskmanager.entity.ExamQuestion;
import com.taskmanager.entity.ExamTopic;
import com.taskmanager.repository.ExamPathRepository;
import com.taskmanager.repository.ExamPhaseRepository;
import com.taskmanager.repository.ExamQuestionRepository;
import com.taskmanager.repository.ExamTopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class ExamPathSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ExamPathSeeder.class);

    private final ExamPathRepository pathRepo;
    private final ExamPhaseRepository phaseRepo;
    private final ExamTopicRepository topicRepo;
    private final ExamQuestionRepository questionRepo;

    public ExamPathSeeder(ExamPathRepository pathRepo,
                          ExamPhaseRepository phaseRepo,
                          ExamTopicRepository topicRepo,
                          ExamQuestionRepository questionRepo) {
        this.pathRepo = pathRepo;
        this.phaseRepo = phaseRepo;
        this.topicRepo = topicRepo;
        this.questionRepo = questionRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (pathRepo.findBySlug("tnpsc-group-4").isPresent()) {
            log.info("Exam paths already seeded, skipping.");
            return;
        }
        seedTnpscGroup4();
        seedComingSoon();
        log.info("Exam paths seeded successfully.");
    }

    private void seedTnpscGroup4() {
        ExamPath path = new ExamPath();
        path.setTitle("TNPSC Group 4");
        path.setSlug("tnpsc-group-4");
        path.setCategory("TNPSC");
        path.setDescription("Complete preparation path for TNPSC Group 4 exam. Follow all 132 tasks and crack your government job.");
        path.setIcon("TN");
        path.setAudience("Class 12 Pass, Graduates, Age 18-30");
        path.setLanguage("Tamil & English");
        path.setTotalWeeks(24);
        path.setTotalTasks(132);
        path.setComingSoon(false);
        path = pathRepo.save(path);

        // ── Phase 1: Foundation (42 topics) ──────────────────────────────
        ExamPhase phase1 = new ExamPhase();
        phase1.setExamPath(path);
        phase1.setTitle("Phase 1: Foundation");
        phase1.setDescription("Build your core knowledge base across all major subjects");
        phase1.setIcon("P1");
        phase1.setOrderIndex(1);
        phase1.setTotalTopics(42);
        phase1 = phaseRepo.save(phase1);

        saveTopics(phase1, Arrays.asList(
                new String[]{"Indian Polity - Constitution Basics", "Preamble, features of Indian Constitution, sources", "45", "true"},
                new String[]{"Indian Polity - Fundamental Rights", "Articles 12-35, Right to Equality, Freedom, Exploitation", "50", "true"},
                new String[]{"Indian Polity - Directive Principles", "DPSP Articles 36-51, differences from Fundamental Rights", "40", "true"},
                new String[]{"Indian Polity - Parliament", "Lok Sabha, Rajya Sabha, powers, sessions, bills", "55", "true"},
                new String[]{"Indian Polity - President and Vice President", "Election, powers, functions, impeachment", "45", "true"},
                new String[]{"Indian Polity - Prime Minister and Council", "Formation, powers, collective responsibility", "40", "true"},
                new String[]{"Indian Polity - Judiciary", "Supreme Court, High Court, structure and jurisdiction", "50", "true"},
                new String[]{"Indian Polity - State Government", "Governor, CM, State Legislature, relations with Centre", "45", "true"},
                new String[]{"Indian Polity - Local Government", "73rd and 74th Amendment, Panchayati Raj, Urban bodies", "40", "true"},
                new String[]{"Indian Polity - Elections", "Election Commission, EVM, electoral process in India", "35", "true"},
                new String[]{"Indian History - Ancient India", "Indus Valley, Vedic period, Maurya Empire", "55", "true"},
                new String[]{"Indian History - Medieval India", "Delhi Sultanate, Mughal Empire, Vijayanagara", "55", "true"},
                new String[]{"Indian History - Modern India I", "European arrival, British rule, 1857 revolt", "50", "true"},
                new String[]{"Indian History - Modern India II", "Reform movements, Indian National Congress", "50", "true"},
                new String[]{"Indian History - Freedom Struggle", "Gandhi, Non-cooperation, Civil Disobedience, Quit India", "60", "true"},
                new String[]{"Indian History - Post Independence", "Partition, integration of states, Constitution adoption", "45", "true"},
                new String[]{"Tamil History - Sangam Age", "Sangam literature, three kingdoms, Chera, Chola, Pandya", "50", "true"},
                new String[]{"Tamil History - Medieval Tamil Nadu", "Pallava, Later Chola, Vijayanagara in Tamil Nadu", "45", "true"},
                new String[]{"Tamil History - Modern Tamil Nadu", "British in Tamil Nadu, freedom fighters from Tamil Nadu", "40", "true"},
                new String[]{"Geography - Physical Geography of India", "Himalayas, Plains, Plateaus, Rivers, Climate zones", "55", "true"},
                new String[]{"Geography - Indian Climate", "Monsoon, seasons, rainfall distribution, cyclones", "45", "true"},
                new String[]{"Geography - Agriculture in India", "Kharif/Rabi, major crops, irrigation, Green Revolution", "50", "true"},
                new String[]{"Geography - Natural Resources", "Forests, minerals, soil types, conservation", "45", "true"},
                new String[]{"Geography - Tamil Nadu Geography", "Districts, rivers, dams, forests, wildlife sanctuaries", "50", "true"},
                new String[]{"Economics - Basic Concepts", "Micro vs Macro, demand, supply, market types", "40", "true"},
                new String[]{"Economics - Indian Economy Overview", "Mixed economy, Five Year Plans, NITI Aayog", "45", "true"},
                new String[]{"Economics - Banking and Finance", "RBI, commercial banks, monetary policy, NBFC", "50", "true"},
                new String[]{"Economics - Government Budget", "Union Budget, fiscal policy, taxes, deficit", "45", "true"},
                new String[]{"Economics - Poverty and Employment", "BPL, MGNREGA, employment schemes, poverty alleviation", "40", "true"},
                new String[]{"General Science - Physics Basics", "Motion, force, work, energy, sound, light", "55", "true"},
                new String[]{"General Science - Chemistry Basics", "Elements, compounds, acids, bases, periodic table", "55", "true"},
                new String[]{"General Science - Biology Basics", "Cell, tissues, human body systems, nutrition", "55", "true"},
                new String[]{"General Science - Environment", "Ecosystem, food chain, biodiversity, pollution", "45", "true"},
                new String[]{"Current Affairs - National", "Important national events, schemes, awards", "30", "true"},
                new String[]{"Current Affairs - Tamil Nadu", "State govt schemes, appointments, events", "30", "true"},
                new String[]{"Current Affairs - International", "Major world events, India foreign relations", "30", "true"},
                new String[]{"Aptitude - Number System", "LCM, HCF, fractions, decimals, simplification", "45", "true"},
                new String[]{"Aptitude - Percentage and Profit", "Percentage, profit and loss, discount calculations", "45", "true"},
                new String[]{"Aptitude - Ratio and Proportion", "Ratio, proportion, variation, partnership", "40", "true"},
                new String[]{"Aptitude - Time Speed and Distance", "Problems on trains, boats, streams", "45", "true"},
                new String[]{"Reasoning - Series and Patterns", "Number series, letter series, analogy, classification", "40", "true"},
                new String[]{"Reasoning - Coding and Decoding", "Coding-decoding, blood relations, directions", "40", "true"}
        ));

        // ── Phase 2: Core Studies (70 topics) ────────────────────────────
        ExamPhase phase2 = new ExamPhase();
        phase2.setExamPath(path);
        phase2.setTitle("Phase 2: Core Studies");
        phase2.setDescription("Deep dive into high-weightage topics with practice questions");
        phase2.setIcon("P2");
        phase2.setOrderIndex(2);
        phase2.setTotalTopics(70);
        phase2 = phaseRepo.save(phase2);

        saveTopics(phase2, Arrays.asList(
                new String[]{"Polity - Constitutional Amendments", "Important amendments 42nd 44th 52nd 61st 73rd 74th 86th 101st", "50", "true"},
                new String[]{"Polity - Emergency Provisions", "National, State and Financial Emergency, Article 356", "45", "true"},
                new String[]{"Polity - Constitutional Bodies", "CAG, UPSC, Finance Commission, Election Commission", "50", "true"},
                new String[]{"Polity - Statutory Bodies", "NHRC, CBI, CVC, Lokpal, RTI Act", "45", "true"},
                new String[]{"Polity - Centre-State Relations", "Legislative, administrative, financial relations", "45", "true"},
                new String[]{"Polity - Schedules of Constitution", "All 12 schedules and their significance", "40", "true"},
                new String[]{"Polity - Fundamental Duties", "Article 51A, 11 fundamental duties, significance", "35", "true"},
                new String[]{"History - Indus Valley Civilization", "Cities, trade, religion, decline theories", "50", "true"},
                new String[]{"History - Vedic Period", "Early and Later Vedic, society, economy, religion", "50", "true"},
                new String[]{"History - Buddhism and Jainism", "Teachings, spread, councils, differences", "45", "true"},
                new String[]{"History - Maurya Empire", "Chandragupta, Ashoka, administration, decline", "55", "true"},
                new String[]{"History - Gupta Empire", "Golden age, art, literature, science achievements", "50", "true"},
                new String[]{"History - South Indian Kingdoms", "Satavahanas, Pallavas, Rashtrakutas, Chalukyas", "55", "true"},
                new String[]{"History - Delhi Sultanate", "Five dynasties, administration, cultural contributions", "55", "true"},
                new String[]{"History - Mughal Empire", "Babur to Aurangzeb, administration, art, decline", "60", "true"},
                new String[]{"History - Maratha Empire", "Shivaji, administration, Peshwas, fall", "50", "true"},
                new String[]{"History - European Companies in India", "Portuguese, Dutch, French, British trade to rule", "50", "true"},
                new String[]{"History - Social Reform Movements", "Raja Ram Mohan Roy, Vivekananda, Periyar, Ambedkar", "55", "true"},
                new String[]{"History - Revolutionary Movements", "Bal Gangadhar Tilak, Bhagat Singh, INA, Subhash Bose", "50", "true"},
                new String[]{"Geography - World Geography Basics", "Continents, oceans, latitude, longitude, time zones", "50", "true"},
                new String[]{"Geography - Atmosphere and Climate", "Layers, pressure, winds, clouds, precipitation", "50", "true"},
                new String[]{"Geography - Ocean and Landforms", "Ocean currents, tides, erosion, plateaus, plains", "45", "true"},
                new String[]{"Geography - India Neighbours", "China, Pakistan, Nepal, Bhutan, Bangladesh, Sri Lanka", "45", "true"},
                new String[]{"Geography - Transport in India", "Railways, roadways, waterways, airways", "45", "true"},
                new String[]{"Geography - Industries in India", "Iron and steel, textile, petrochemical, IT industry", "50", "true"},
                new String[]{"Geography - Population and Census", "Census 2011, density, literacy, sex ratio, migration", "45", "true"},
                new String[]{"Economics - National Income", "GDP, GNP, NNP, per capita income, HDI", "50", "true"},
                new String[]{"Economics - Agriculture Economics", "Land reforms, MSP, PDS, food security, APMC", "50", "true"},
                new String[]{"Economics - Industry and Trade", "Industrial policy, SEZ, FDI, exports, WTO", "50", "true"},
                new String[]{"Economics - Inflation and Money", "Types of inflation, CPI, WPI, monetary measures", "45", "true"},
                new String[]{"Economics - Taxation System", "Direct vs indirect, GST, income tax, corporate tax", "50", "true"},
                new String[]{"Economics - Government Schemes", "PM-Kisan, Jan Dhan, Mudra, Atmanirbhar, Startup India", "45", "true"},
                new String[]{"Science - Physics Electricity", "Current, voltage, resistance, Ohm law, circuits", "55", "true"},
                new String[]{"Science - Physics Optics", "Reflection, refraction, lenses, human eye, defects", "50", "true"},
                new String[]{"Science - Physics Magnetism", "Magnets, electromagnetism, motors, generators", "45", "true"},
                new String[]{"Science - Chemistry Chemical Reactions", "Types, balancing equations, acids, bases, pH", "55", "true"},
                new String[]{"Science - Chemistry Metals and Non-metals", "Properties, reactivity series, alloys, corrosion", "50", "true"},
                new String[]{"Science - Chemistry Carbon Compounds", "Organic chemistry basics, hydrocarbons, polymers", "50", "true"},
                new String[]{"Science - Biology Cell Biology", "Cell structure, organelles, mitosis, meiosis", "55", "true"},
                new String[]{"Science - Biology Human Physiology", "Digestive, circulatory, respiratory, nervous system", "60", "true"},
                new String[]{"Science - Biology Plant Kingdom", "Classification, photosynthesis, reproduction in plants", "50", "true"},
                new String[]{"Science - Biology Genetics", "DNA, RNA, heredity, Mendel laws, genetic disorders", "55", "true"},
                new String[]{"Science - Diseases and Health", "Communicable diseases, vitamins, deficiencies, vaccines", "50", "true"},
                new String[]{"Science - Technology and Space", "ISRO missions, satellite types, recent launches", "45", "true"},
                new String[]{"Current Affairs - Science and Technology", "Recent developments, inventions, discoveries", "30", "true"},
                new String[]{"Current Affairs - Economy and Finance", "Budget highlights, economic survey, RBI policy", "30", "true"},
                new String[]{"Current Affairs - Sports and Awards", "National, international awards, sports events", "30", "true"},
                new String[]{"Aptitude - Algebra", "Linear equations, quadratic, polynomials", "50", "true"},
                new String[]{"Aptitude - Geometry", "Lines, angles, triangles, circles, area, volume", "55", "true"},
                new String[]{"Aptitude - Data Interpretation", "Tables, bar charts, pie charts, line graphs", "50", "true"},
                new String[]{"Aptitude - Average and Mixtures", "Simple average, weighted average, alligation", "45", "true"},
                new String[]{"Aptitude - Simple and Compound Interest", "SI, CI formulas, EMI problems", "45", "true"},
                new String[]{"Aptitude - Mensuration", "2D and 3D shapes, surface area, volume problems", "50", "true"},
                new String[]{"Reasoning - Syllogism", "Statements, conclusions, Venn diagram method", "45", "true"},
                new String[]{"Reasoning - Puzzles", "Seating arrangement, scheduling, grid puzzles", "50", "true"},
                new String[]{"Reasoning - Logical Deduction", "Cause-effect, assumption, inference, argument", "45", "true"},
                new String[]{"English - Grammar Basics", "Parts of speech, tenses, voice, narration", "45", "true"},
                new String[]{"English - Vocabulary", "Synonyms, antonyms, one-word substitution, idioms", "40", "true"},
                new String[]{"English - Comprehension", "Reading passage, inference, title, tone questions", "45", "true"},
                new String[]{"English - Error Detection", "Spotting errors, sentence correction, fill blanks", "40", "true"},
                new String[]{"Mock Test 1 - Polity and History", "Full revision test 50 MCQs", "60", "true"},
                new String[]{"Mock Test 2 - Geography and Economics", "Full revision test 50 MCQs", "60", "true"},
                new String[]{"Mock Test 3 - Science and Aptitude", "Full revision test 50 MCQs", "60", "true"},
                new String[]{"Mock Test 4 - Current Affairs and Reasoning", "Full revision test 50 MCQs", "60", "true"},
                new String[]{"Mock Test 5 - Full Syllabus Test 1", "100 MCQs covering all subjects", "90", "true"},
                new String[]{"Mock Test 6 - Full Syllabus Test 2", "100 MCQs covering all subjects", "90", "true"},
                new String[]{"Mock Test 7 - Full Syllabus Test 3", "100 MCQs covering all subjects", "90", "true"},
                new String[]{"Mock Test 8 - Final Grand Test", "200 MCQs Full TNPSC Group 4 pattern", "120", "true"}
        ));

        // ── Phase 3: Tamil and Culture (20 topics) ────────────────────────
        ExamPhase phase3 = new ExamPhase();
        phase3.setExamPath(path);
        phase3.setTitle("Phase 3: Tamil and Culture");
        phase3.setDescription("Tamil language, literature, culture and Tamil Nadu specific topics");
        phase3.setIcon("P3");
        phase3.setOrderIndex(3);
        phase3.setTotalTopics(20);
        phase3 = phaseRepo.save(phase3);

        saveTopics(phase3, Arrays.asList(
                new String[]{"Tamil Literature - Sangam Period", "Ettuthokai, Pattuppattu, Tolkappiyam overview", "50", "true"},
                new String[]{"Tamil Literature - Post-Sangam", "Thirukkural, Silappathikaram, Manimekalai", "55", "true"},
                new String[]{"Tamil Literature - Medieval Period", "Devaram, Divya Prabandham, Periya Puranam", "50", "true"},
                new String[]{"Tamil Literature - Modern Period", "Bharathiyar, Bharathidasan, modern Tamil writers", "45", "true"},
                new String[]{"Tamil Grammar - Tolkappiyam", "Eluthu, Col, Porul three sections overview", "45", "true"},
                new String[]{"Tamil Grammar - Parts of Speech", "Peyar, Vinai, Idai, Uri noun verb adverb adjective", "50", "true"},
                new String[]{"Tamil Grammar - Sentence Structure", "Thodai, Ilakkana, Kalai grammar rules", "45", "true"},
                new String[]{"Tamil Culture - Festivals", "Pongal, Karthigai, Thai Pusam, temple festivals", "40", "true"},
                new String[]{"Tamil Culture - Art Forms", "Bharatanatyam, Kolattam, Kavadi, folk arts", "40", "true"},
                new String[]{"Tamil Culture - Music", "Carnatic music, Thiruvaiyaru, composers, ragas", "45", "true"},
                new String[]{"Tamil Nadu - Districts and Administration", "All 38 districts, headquarters, notable features", "45", "true"},
                new String[]{"Tamil Nadu - Rivers and Dams", "Cauvery, Vaigai, Tamirabarani, major dams", "45", "true"},
                new String[]{"Tamil Nadu - Wildlife and Forests", "Mudumalai, Anamalai, Guindy, wildlife sanctuaries", "40", "true"},
                new String[]{"Tamil Nadu - Government Schemes", "State government welfare schemes, CM initiatives", "40", "true"},
                new String[]{"Tamil Nadu - Economy", "Agriculture, industries, IT, ports, trade", "45", "true"},
                new String[]{"Tamil Nadu - Famous Personalities", "Scientists, leaders, artists, sportspersons from TN", "40", "true"},
                new String[]{"Tamil Proverbs and One-liners", "Important proverbs, their meaning and usage", "35", "true"},
                new String[]{"Tamil - Reading Comprehension", "Passage reading, inference, summary in Tamil", "45", "true"},
                new String[]{"Tamil - Letter and Essay Writing", "Official letter, application, essay formats in Tamil", "40", "true"},
                new String[]{"Final Revision - Tamil and Culture", "Complete revision of all Tamil topics with MCQs", "60", "true"}
        ));
    }

    private void saveTopics(ExamPhase phase, List<String[]> topicData) {
        for (int i = 0; i < topicData.size(); i++) {
            String[] data = topicData.get(i);
            ExamTopic topic = new ExamTopic();
            topic.setPhase(phase);
            topic.setTitle(data[0]);
            topic.setDescription(data[1]);
            topic.setContent("");
            topic.setResourceUrl(null);
            topic.setResourceType(null);
            topic.setOrderIndex(i + 1);
            topic.setXpReward(20);
            topic.setEstimatedMinutes(Integer.parseInt(data[2]));
            topic.setHasQuiz(Boolean.parseBoolean(data[3]));
            topic = topicRepo.save(topic);

            if (phase.getOrderIndex() == 1 && i == 0) {
                seedSampleQuestions(topic);
            }
        }
    }

    private void seedSampleQuestions(ExamTopic topic) {
        ExamQuestion q1 = new ExamQuestion();
        q1.setTopic(topic);
        q1.setOrderIndex(1);
        q1.setQuestion("When was the Indian Constitution adopted?");
        q1.setOptionA("15 August 1947");
        q1.setOptionB("26 January 1950");
        q1.setOptionC("26 November 1949");
        q1.setOptionD("30 January 1948");
        q1.setCorrectOption("C");
        q1.setExplanation("The Constitution was adopted on 26 November 1949 and came into effect on 26 January 1950.");

        ExamQuestion q2 = new ExamQuestion();
        q2.setTopic(topic);
        q2.setOrderIndex(2);
        q2.setQuestion("Who is called the Father of the Indian Constitution?");
        q2.setOptionA("Jawaharlal Nehru");
        q2.setOptionB("Sardar Vallabhbhai Patel");
        q2.setOptionC("B.R. Ambedkar");
        q2.setOptionD("Rajendra Prasad");
        q2.setCorrectOption("C");
        q2.setExplanation("Dr. B.R. Ambedkar was the chairman of the Drafting Committee.");

        ExamQuestion q3 = new ExamQuestion();
        q3.setTopic(topic);
        q3.setOrderIndex(3);
        q3.setQuestion("How many articles were in the original Indian Constitution?");
        q3.setOptionA("395");
        q3.setOptionB("444");
        q3.setOptionC("448");
        q3.setOptionD("400");
        q3.setCorrectOption("A");
        q3.setExplanation("The original Constitution had 395 articles in 22 parts and 8 schedules.");

        questionRepo.saveAll(Arrays.asList(q1, q2, q3));
    }

    private void seedComingSoon() {
        ExamPath upsc = new ExamPath();
        upsc.setTitle("UPSC Civil Services");
        upsc.setSlug("upsc-civil-services");
        upsc.setCategory("UPSC");
        upsc.setDescription("2-year roadmap for IAS/IPS preparation");
        upsc.setIcon("UP");
        upsc.setAudience("Graduates, Age 21-32");
        upsc.setLanguage("English & Hindi");
        upsc.setTotalWeeks(96);
        upsc.setTotalTasks(0);
        upsc.setComingSoon(true);
        pathRepo.save(upsc);

        ExamPath cat = new ExamPath();
        cat.setTitle("CAT MBA");
        cat.setSlug("cat-mba");
        cat.setCategory("MBA");
        cat.setDescription("6-month preparation plan for CAT exam");
        cat.setIcon("CA");
        cat.setAudience("Graduates, Working Professionals");
        cat.setLanguage("English");
        cat.setTotalWeeks(24);
        cat.setTotalTasks(0);
        cat.setComingSoon(true);
        pathRepo.save(cat);

        ExamPath grp2 = new ExamPath();
        grp2.setTitle("TNPSC Group 2");
        grp2.setSlug("tnpsc-group-2");
        grp2.setCategory("TNPSC");
        grp2.setDescription("Comprehensive path for TNPSC Group 2 exam");
        grp2.setIcon("T2");
        grp2.setAudience("Graduates, Age 18-35");
        grp2.setLanguage("Tamil & English");
        grp2.setTotalWeeks(32);
        grp2.setTotalTasks(0);
        grp2.setComingSoon(true);
        pathRepo.save(grp2);
    }
}