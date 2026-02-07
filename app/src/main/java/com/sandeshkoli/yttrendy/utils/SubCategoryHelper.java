package com.sandeshkoli.yttrendy.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class SubCategoryHelper {

    public static Map<String, String> getSubCategories(String mainCategory) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        switch (mainCategory) {
            case "📺 TV Shows":
                map.put("India TV Shows", "Indian TV serials highlights 2026");
                map.put("Netflix Originals", "Netflix best original series trailers");
                map.put("HBO Series", "HBO top rated series clips");
                map.put("Popular TV Series", "Global trending TV shows 2026");
                map.put("Disney+ Shows", "Disney plus hotstar trending");
                map.put("Amazon Prime Video", "Amazon Prime Video best series");
                map.put("Crime Series", "Best crime thriller series clips");
                map.put("Sci-Fi Series", "Top sci-fi web series trailers");
                map.put("Fantasy Series", "Epic fantasy series 2026");
                map.put("BBC Shows", "BBC world best documentaries and shows");
                map.put("Apple TV+", "Apple TV plus best series trailers");
                break;

            case "🎵 Music":
                map.put("Bollywood Hits", "New bollywood songs 2026");
                map.put("Punjabi Songs", "Latest punjabi songs 2026");
                map.put("Kishore Kumar Hits", "Kishore Kumar best romantic songs jukebox");
                map.put("Lata Mangeshkar", "Lata Mangeshkar evergreen old songs");
                map.put("90s Bollywood Hits", "Best 90s hindi songs 4k");
                map.put("Old is Gold", "Evergreen old hindi songs collection");
                map.put("Mohammed Rafi", "Mohammed Rafi best emotional hits");
                map.put("Lo-Fi Music", "Lofi study music slow reverb");
                map.put("Indian Hip Hop", "Best indian rap songs hindi");
                map.put("Devotional", "Bhakti songs and bhajans hindi");
                map.put("Indie Music", "Best indian indie artists");
                map.put("English Pop", "Top global billboard hits 2026");
                break;

            case "⛩️ Anime":
                map.put("Popular Anime", "Most popular anime 2026 clips");
                map.put("Anime Episodes", "Anime latest episodes hindi english");
                map.put("Studio Ghibli", "Studio Ghibli movie best moments");
                map.put("Anime Movies", "Top rated anime movies 2026");
                map.put("Shonen Anime", "Shonen jump anime highlights");
                map.put("Action Anime", "Best action anime fight scenes");
                map.put("Romance Anime", "Sweet romantic anime clips");
                map.put("Slice of Life Anime", "Best slice of life anime moments");
                map.put("Manga", "Popular manga news and reviews");
                break;

            case "🎮 Games":
                map.put("India Gaming", "Top indian gaming streamers live");
                map.put("Popular Games", "Trending games 2026 gameplay");
                map.put("Gaming Streams", "Live esports tournaments india");
                map.put("Fortnite", "Fortnite amazing kills and highlights");
                map.put("Minecraft", "Minecraft speedrun and building hindi");
                map.put("Call of Duty", "COD Warzone latest gameplay");
                map.put("GTA", "GTA 5 and GTA 6 leaks news");
                map.put("Valorant", "Valorant pro player highlights");
                map.put("League of Legends", "LOL best plays and tournaments");
                map.put("Esports", "Global esports news and matches");
                map.put("Game Reviews", "New game reviews and walkthroughs");
                map.put("Gaming Tips", "How to level up in games tips");
                break;

            case "⚽ Sports":
                map.put("India Sports", "Indian sports team highlights");
                map.put("NBA", "NBA best dunks and match highlights");
                map.put("NFL", "NFL top plays and superbowl news");
                map.put("Premier League", "Premier league goals highlights 2026");
                map.put("UEFA Champions League", "UCL match highlights and goals");
                map.put("Football", "World football top moments");
                map.put("Cricket", "International cricket highlights 2026");
                map.put("Formula 1", "F1 race highlights and podiums");
                map.put("IPL", "IPL 2026 latest matches highlights");
                map.put("UFC", "UFC best knockouts and fights");
                map.put("WWE", "WWE Raw and Smackdown highlights");
                map.put("MLB", "MLB baseball top plays");
                map.put("NHL", "NHL ice hockey highlights");
                map.put("Olympics", "Olympics best moments highlights");
                map.put("Tennis", "Tennis grand slam highlights");
                map.put("Boxing", "Pro boxing latest fight highlights");
                map.put("Motorsport", "MotoGP and Rally race highlights");
                map.put("Racing", "Horse racing and car racing results");
                map.put("Hockey", "International field hockey highlights");
                map.put("Golf", "PGA tour golf best shots");
                map.put("Rugby", "Rugby world cup highlights");
                map.put("Athletics", "World athletics championship moments");
                break;

            case "🎙️ Journalists":
                // Balance: Popular Indian, Global and Regional
                map.put("Sudhir Chaudhary", "Sudhir Chaudhary latest DNA analysis");
                map.put("Arnab Goswami", "Arnab Goswami Republic Bharat debate");
                map.put("Rajat Sharma", "Rajat Sharma Aap Ki Adalat latest");
                map.put("Palki Sharma", "Palki Sharma Vantage analysis");
                map.put("Sushant Sinha", "Sushant Sinha News Ki Pathshala latest");
                map.put("Deepak Chaurasia", "Deepak Chaurasia news debate");
                map.put("Aman Chopra", "Aman Chopra Desh Nahin Jhukne Denge");
                map.put("Rubika Liyaquat", "Rubika Liyaquat latest news debate");
                map.put("Top Journalists", "World top investigative journalists");
                break;

            case "🕌 Scholars":
                // Generic Wisdom & Religion mix
                map.put("Swami Vivekananda", "Swami Vivekananda motivational speech hindi");
                map.put("Premanand Ji Maharaj", "Premanand Ji Maharaj Vrindavan satsang");
                map.put("Sadhguru", "Sadhguru wisdom and meditation hindi");
                map.put("Gaur Gopal Das", "Gaur Gopal Das life lessons");
                map.put("Jaya Kishori", "Jaya Kishori motivational katha");
                map.put("Aniruddhacharya Ji", "Aniruddhacharya ji funny and motivation");
                map.put("Adi Shankaracharya", "Adi Shankaracharya story and wisdom");
                map.put("Dharma Wisdom", "Sanatan Dharma spiritual knowledge");
                break;

            case "🌍 Leaders":
                map.put("Narendra Modi", "PM Narendra Modi latest news");
                map.put("Donald Trump", "Donald Trump latest updates");
                map.put("Joe Biden", "President Joe Biden news");
                map.put("Xi Jinping", "China President Xi Jinping news");
                map.put("Vladimir Putin", "Russia President Putin updates");
                map.put("Rishi Sunak", "Rishi Sunak UK politics");
                map.put("Emmanuel Macron", "Emmanuel Macron France politics");
                map.put("Olaf Scholz", "Olaf Scholz Germany news");
                map.put("Fumio Kishida", "Fumio Kishida Japan updates");
                map.put("Justin Trudeau", "Justin Trudeau Canada news");
                map.put("Recep Tayyip Erdogan", "Erdogan Turkey news");
                map.put("Imran Khan", "Imran Khan latest video");
                map.put("Shehbaz Sharif", "PM Shehbaz Sharif updates");
                map.put("Giorgia Meloni", "Giorgia Meloni Italy news");
                break;

            case "✨ Motivational":
                map.put("Shivaji Maharaj", "Chatrapati Shivaji Maharaj motivational history");
                map.put("Sambhaji Maharaj", "Chatrapati Sambhaji Maharaj bravery life story");
                map.put("Maharana Pratap", "Maharana Pratap bravery history");
                map.put("Bhagat Singh", "Bhagat Singh krantikari documentary");
                map.put("Prithviraj Chauhan", "Prithviraj Chauhan battle history");
                map.put("Subhash Chandra Bose", "Netaji Subhash Chandra Bose motivation");
                map.put("Real Heroes", "Indian warriors and freedom fighters history");
                map.put("Life Lessons", "Inspirational life stories of great leaders");
                break;

            case "🎓 Courses":
                map.put("Digital Marketing", "Digital marketing full course hindi");
                map.put("Graphic Designing", "Learn Photoshop Illustrator hindi");
                map.put("Adobe Photoshop", "Photoshop advanced tutorials");
                map.put("YouTube SEO", "Grow on YouTube 2026 tips");
                map.put("Hisham Sarwar", "Hisham Sarwar freelancing tips");
                map.put("GFX Mentor", "GFX Mentor design course");
                map.put("Freelancing", "How to earn money online freelancing");
                map.put("Adobe Illustrator", "Illustrator full course hindi");
                break;

            case "💻 Programming":
                map.put("Python", "Python programming course");
                map.put("JavaScript", "JavaScript tutorial for beginners");
                map.put("React Native", "React Native full tutorial");
                map.put("Flutter", "Flutter app development");
                map.put("Java", "Java programming tutorials");
                map.put("HTML/CSS", "HTML CSS web design hindi");
                map.put("NodeJS", "Node JS backend tutorial");
                map.put("Angular", "Angular JS full course");
                map.put("MongoDB", "MongoDB database tutorial");
                map.put("Firebase", "Firebase android tutorial");
                map.put("C++", "C plus plus tutorial");
                map.put("MySQL", "SQL database tutorials");
                break;

            case "🧪 Tech":
                map.put("India Tech", "Latest technology news india");
                map.put("Tech Reviews", "Gadget and mobile reviews 2026");
                map.put("Apple Products", "iPhone 17 and Macbook leaks");
                map.put("Smartphones", "Best smartphones under 25000");
                map.put("Samsung", "Samsung S26 Ultra news");
                map.put("Google Products", "Pixel 10a and Google AI news");
                map.put("Gaming Tech", "Best gaming hardware 2026");
                map.put("AI & Machine Learning", "ChatGPT and AI future tech");
                break;

            case "🥘 Cooking":
                map.put("India Cooking", "Best indian recipes in hindi");
                map.put("MasterChef", "MasterChef best dishes clips");
                map.put("Gordon Ramsay", "Gordon Ramsay cooking masterclass");
                map.put("Cooking Shows", "Popular cooking competitions clips");
                map.put("Italian Cuisine", "Authentic italian pasta recipes");
                map.put("Japanese Cuisine", "Sushi and ramen making tutorials");
                map.put("Baking", "Cake and pastry baking tips");
                map.put("Desserts", "Easy sweet dish recipes hindi");
                break;

            case "👶 Kids":
                map.put("Kids Poems", "Cocomelon and nursery rhymes");
                map.put("Johnny Johnny", "Johnny Johnny yes papa all versions");
                map.put("Oggy & Cockroaches", "Oggy and the cockroaches best episodes");
                map.put("Tom and Jerry", "Tom and Jerry classic episodes");
                map.put("Mr Bean", "Mr Bean cartoon and live action");
                map.put("Baby Shark", "Baby shark dance and kids songs");
                map.put("Bugs Bunny", "Looney Tunes Bugs Bunny clips");
                map.put("Dragon Ball Z", "DBZ best fight scenes highlights");
                map.put("Ben 10", "Ben 10 all alien force episodes");
                map.put("Motu Patlu", "Motu Patlu latest funny episodes");
                break;

            case "🔍 Leaks":
                map.put("Viral Video", "Trending viral video social media");
                map.put("Leaked Video", "Latest leaked news and footage");
                map.put("Exposed", "Exposing social media scams clips");
                map.put("Politicians", "Politicians leaked statements news");
                break;
            case "📰 News":
                map.put("Latest News", "breaking news india today live");
                map.put("International", "world news english latest headlines");
                map.put("Politics News", "indian politics analysis and updates");
                map.put("Business News", "stock market and budget news india");
                map.put("Crime News", "crime patrol style real news reports");
                map.put("Science News", "latest space and science discoveries");
                break;
            case "✈️ Travel":
                map.put("India Vlogs", "best travel vlogs india places");
                map.put("Budget Trips", "how to travel india cheap tips");
                map.put("Mountains", "manali ladakh spiti travel guide");
                map.put("International", "europe and asia tour vlogs hindi");
                map.put("Hidden Places", "unexplored beautiful places in india");
                map.put("Solo Travel", "solo traveling tips and experience");
                break;

            case "😂 Comedy":
                map.put("Stand-up India", "best indian standup comedy 2026");
                map.put("Kapil Sharma Show", "the kapil sharma show latest clips");
                map.put("Funny Pranks", "best funny pranks india viral");
                map.put("Roast Battles", "carryminati style roast videos hindi");
                map.put("Vine/Skits", "funny vines and short skits hindi");
                break;
            case "🎭 Dramas":
                map.put("Indian Serials", "top indian drama serials highlights");
                map.put("Pakistani Dramas", "pakistani dramas best episodes highlights");
                map.put("Web Series", "best indian web series trailers 2026");
                map.put("Turkish Dubbed", "turkish dramas in hindi full episodes");
                map.put("Retro Dramas", "classic old indian TV shows");
                break;

            case "👗 Lifestyle":
                map.put("Home Decor", "modern home interior design ideas");
                map.put("Fashion Tips", "mens and womens fashion trends 2026");
                map.put("Minimalism", "minimalist living and organizing tips");
                map.put("Self Care", "daily routine and self care habits");
                map.put("DIY Projects", "best creative diy home projects");
                map.put("Productivity", "how to manage time and be productive");
                break;

            case "India Now":
                map.put("Trending Now", "Trending india today viral");
                map.put("India News", "Live news india breaking");
                map.put("India Sports", "Indian cricket team news");
                map.put("India Gaming", "Top indian gamers live");
                map.put("India Lifestyle", "Indian culture and travel vlogs");
                break;
            case "💪 Fitness":
                map.put("Home Workout", "Full body workout at home");
                map.put("Yoga", "Yoga for beginners");
                map.put("Diet Plans", "Weight loss diet plan india");
                map.put("Bodybuilding", "Gym motivation status");
                break;

            default:
                map.put("🔥 Viral Hits", mainCategory + " viral trending videos 2026");
                map.put("😱 Shocking Moments", mainCategory + " shocking and amazing moments");
                map.put("🏆 World Records", "best " + mainCategory + " world record breaking clips");
                map.put("🕵️ Unseen Clips", mainCategory + " behind the scenes unseen footage");
                map.put("😂 Epic Fails", mainCategory + " funniest epic fails collection");
                map.put("💎 Hidden Gems", "underrated high quality " + mainCategory + " videos");
                map.put("⚠️ Don't Miss", "must watch " + mainCategory + " special content");
                break;
        }
        return map;
    }
}