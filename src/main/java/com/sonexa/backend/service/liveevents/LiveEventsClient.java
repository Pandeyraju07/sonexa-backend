package com.sonexa.backend.service.liveevents;

import com.sonexa.backend.model.dto.CatalogDtos.EventSetlistTrackDto;
import com.sonexa.backend.model.dto.CatalogDtos.EventTicketTierDto;
import com.sonexa.backend.model.dto.CatalogDtos.LiveEventDetailResponse;
import com.sonexa.backend.model.dto.CatalogDtos.LiveEventDto;
import com.sonexa.backend.model.dto.CatalogDtos.LiveEventsFeedResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LiveEventsClient {

    private final List<LiveEventDto> allEvents = new ArrayList<>();
    private final Set<String> remindedEventIds = Collections.synchronizedSet(new HashSet<>());

    public LiveEventsClient() {
        initEvents();
    }

    private void initEvents() {
        allEvents.clear();

        // 1. Diljit Dosanjh - Dil-Luminati Tour (Mumbai)
        allEvents.add(new LiveEventDto(
                "ev_diljit_mum",
                "Diljit Dosanjh • Dil-Luminati Tour 2026",
                "Diljit Dosanjh",
                "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg",
                "https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=1200&q=80",
                "Jio World Garden, BKC, Mumbai",
                "Mumbai",
                "Sat, 24 Oct 2026",
                "7:00 PM IST",
                "₹1,999",
                "SELLING_FAST",
                "Stadium Tour",
                "https://in.bookmyshow.com",
                false,
                List.of("Diljit Dosanjh", "Special Guest DJ", "Live Dhol Band"),
                List.of(
                        new EventSetlistTrackDto("st_1", "Lover", "Diljit Dosanjh", "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg", "3:12", 192000),
                        new EventSetlistTrackDto("st_2", "GOAT", "Diljit Dosanjh", "https://aac.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-320.mp4", "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg", "3:44", 224000),
                        new EventSetlistTrackDto("st_3", "Born to Shine", "Diljit Dosanjh", "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg", "3:33", 213000),
                        new EventSetlistTrackDto("st_4", "Naina (Crew)", "Diljit Dosanjh", "https://aac.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-320.mp4", "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg", "3:00", 180000)
                )
        ));

        // 2. Arijit Singh - Live in Symphony (Delhi NCR)
        allEvents.add(new LiveEventDto(
                "ev_arijit_del",
                "Arijit Singh • Symphony World Tour Live",
                "Arijit Singh",
                "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg",
                "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1200&q=80",
                "Jawaharlal Nehru Stadium, New Delhi",
                "Delhi NCR",
                "Sun, 15 Nov 2026",
                "6:30 PM IST",
                "₹2,499",
                "SELLING_FAST",
                "Stadium Tour",
                "https://in.bookmyshow.com",
                false,
                List.of("Arijit Singh", "Grand Royal Philharmonic Symphony", "Instrumental Ensemble"),
                List.of(
                        new EventSetlistTrackDto("st_5", "Kesariya", "Arijit Singh", "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg", "4:28", 268000),
                        new EventSetlistTrackDto("st_6", "Tum Hi Ho", "Arijit Singh", "https://aac.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-320.mp4", "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg", "4:22", 262000),
                        new EventSetlistTrackDto("st_7", "Apna Bana Le", "Arijit Singh", "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg", "4:20", 260000)
                )
        ));

        // 3. Sunburn Goa Festival 2026 (Goa)
        allEvents.add(new LiveEventDto(
                "ev_sunburn_goa",
                "Sunburn Goa 2026 • Asia's Premier Dance Festival",
                "Various Global Artists",
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=1200&q=80",
                "Vagator Beach Arena, North Goa",
                "Goa",
                "28-31 Dec 2026",
                "3:00 PM IST Onwards",
                "₹3,999",
                "LIVE_NOW",
                "Festival",
                "https://in.bookmyshow.com",
                false,
                List.of("Martin Garrix", "Hardwell", "Ritviz", "Lost Stories", "Nucleya", "KSHMR"),
                List.of(
                        new EventSetlistTrackDto("st_8", "Liggi", "Ritviz", "https://aac.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-320.mp4", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80", "3:02", 182000),
                        new EventSetlistTrackDto("st_9", "Sage", "Ritviz", "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80", "2:45", 165000)
                )
        ));

        // 4. Karan Aujla - It Was All A Dream Tour (Bengaluru)
        allEvents.add(new LiveEventDto(
                "ev_karan_blr",
                "Karan Aujla • It Was All A Dream Arena Tour",
                "Karan Aujla",
                "https://c.saavncdn.com/artists/Karan_Aujla_003_20230818090514_500x500.jpg",
                "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=1200&q=80",
                "Manpho Convention Center Grounds, Bengaluru",
                "Bengaluru",
                "Fri, 18 Dec 2026",
                "7:30 PM IST",
                "₹1,499",
                "UPCOMING",
                "Stadium Tour",
                "https://in.bookmyshow.com",
                false,
                List.of("Karan Aujla", "Ikky (Producer Live Set)", "Guest Artists"),
                List.of(
                        new EventSetlistTrackDto("st_10", "Tauba Tauba", "Karan Aujla", "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://c.saavncdn.com/artists/Karan_Aujla_003_20230818090514_500x500.jpg", "3:27", 207000),
                        new EventSetlistTrackDto("st_11", "Softly", "Karan Aujla", "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://c.saavncdn.com/artists/Karan_Aujla_003_20230818090514_500x500.jpg", "2:36", 156000),
                        new EventSetlistTrackDto("st_12", "Winning Speech", "Karan Aujla", "https://aac.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-320.mp4", "https://c.saavncdn.com/artists/Karan_Aujla_003_20230818090514_500x500.jpg", "3:01", 181000)
                )
        ));

        // 5. Prateek Kuhad - Silhouettes Acoustic Night (Pune)
        allEvents.add(new LiveEventDto(
                "ev_prateek_pune",
                "Prateek Kuhad • Silhouettes Acoustic Experience",
                "Prateek Kuhad",
                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=1200&q=80",
                "Liberty Square, Phoenix Marketcity, Pune",
                "Pune",
                "Sat, 07 Nov 2026",
                "8:00 PM IST",
                "₹1,299",
                "UPCOMING",
                "Acoustic",
                "https://in.bookmyshow.com",
                false,
                List.of("Prateek Kuhad", "Acoustic Trio Support"),
                List.of(
                        new EventSetlistTrackDto("st_13", "cold/mess", "Prateek Kuhad", "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80", "4:32", 272000),
                        new EventSetlistTrackDto("st_14", "Kasoor", "Prateek Kuhad", "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80", "3:16", 196000)
                )
        ));

        // 6. King - New Life Arena Show (Mumbai)
        allEvents.add(new LiveEventDto(
                "ev_king_mum",
                "King • New Life Arena Tour 2026",
                "King",
                "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80",
                "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=1200&q=80",
                "Dome SVP Stadium, Worli, Mumbai",
                "Mumbai",
                "Sat, 12 Dec 2026",
                "7:00 PM IST",
                "₹999",
                "SELLING_FAST",
                "Stadium Tour",
                "https://in.bookmyshow.com",
                false,
                List.of("King", "Special Guests", "DJ Crew"),
                List.of(
                        new EventSetlistTrackDto("st_15", "Maan Meri Jaan", "King", "https://aac.saavncdn.com/492/Chand-Mera-Dil-Hindi-2024-20241021111624-320.mp4", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80", "3:14", 194000),
                        new EventSetlistTrackDto("st_16", "Tu Aake Dekhle", "King", "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80", "4:40", 280000),
                        new EventSetlistTrackDto("st_17", "Oops", "King", "https://aac.saavncdn.com/832/Gully-Boy-Hindi-2019-20190124110321-320.mp4", "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80", "2:52", 172000)
                )
        ));

        // 7. Bacardi NH7 Weekender (Pune)
        allEvents.add(new LiveEventDto(
                "ev_nh7_pune",
                "NH7 Weekender 2026 • The Happiest Music Festival",
                "Multi-Genre Festival Lineup",
                "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=500&q=80",
                "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=1200&q=80",
                "Mahalakshmi Lawns, Nagar Road, Pune",
                "Pune",
                "04-06 Dec 2026",
                "2:00 PM IST",
                "₹2,199",
                "UPCOMING",
                "Festival",
                "https://in.bookmyshow.com",
                false,
                List.of("The Local Train", "When Chai Met Toast", "Parvaaz", "Seedhe Maut", "DIVINE"),
                List.of(
                        new EventSetlistTrackDto("st_18", "Choo Lo", "The Local Train", "https://aac.saavncdn.com/712/Main-Vaapas-Aaunga-Hindi-2024-20240321154032-320.mp4", "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=500&q=80", "3:53", 233000),
                        new EventSetlistTrackDto("st_19", "Khoj", "When Chai Met Toast", "https://aac.saavncdn.com/264/Love-Exit-Punjabi-2023-20230606132711-320.mp4", "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=500&q=80", "3:25", 205000)
                )
        ));
    }

    public LiveEventsFeedResponse getFeed(String city, String category) {
        List<String> cities = List.of("All", "Mumbai", "Delhi NCR", "Bengaluru", "Goa", "Pune", "Hyderabad", "London", "Dubai", "New York");
        List<String> categories = List.of("All", "Stadium Tour", "Festival", "Acoustic", "Club");

        List<LiveEventDto> filtered = allEvents.stream()
                .filter(e -> city == null || city.equalsIgnoreCase("All") || e.city().equalsIgnoreCase(city))
                .filter(e -> category == null || category.equalsIgnoreCase("All") || e.category().equalsIgnoreCase(category))
                .map(e -> remindedEventIds.contains(e.id()) ? withReminder(e, true) : e)
                .collect(Collectors.toList());

        List<LiveEventDto> featured = allEvents.stream()
                .filter(e -> e.status().equals("SELLING_FAST") || e.status().equals("LIVE_NOW"))
                .map(e -> remindedEventIds.contains(e.id()) ? withReminder(e, true) : e)
                .limit(4)
                .collect(Collectors.toList());

        return new LiveEventsFeedResponse(
                true,
                "Live Concerts & Stadium Tours",
                cities,
                categories,
                featured,
                filtered
        );
    }

    public LiveEventDetailResponse getDetail(String id) {
        LiveEventDto event = allEvents.stream()
                .filter(e -> e.id().equalsIgnoreCase(id))
                .findFirst()
                .orElse(allEvents.get(0));

        boolean isReminded = remindedEventIds.contains(event.id());
        LiveEventDto finalizedEvent = withReminder(event, isReminded);

        List<EventTicketTierDto> tiers = List.of(
                new EventTicketTierDto("tier_silver", "Silver Phase 1 (General Access)", event.priceStarting(), "Access to general standing zone and festival food court.", List.of("General Admission", "Free Parking Zone C"), true),
                new EventTicketTierDto("tier_gold", "Gold Fanpit (Close to Stage)", "₹3,499", "Exclusive entrance, front-of-stage fanpit access, dedicated bar counter.", List.of("Priority Entrance", "Front of Stage Access", "1 Complimentary Drink"), true),
                new EventTicketTierDto("tier_vip", "VIP Platinum Lounge", "₹7,999", "Elevated viewing deck, unlimited gourmet snacks, air-conditioned lounge & artist merchandise kit.", List.of("Elevated Deck View", "Free Flow Beverages & Food", "Exclusive Merch Pack", "Valet Parking"), true)
        );

        List<LiveEventDto> nearby = allEvents.stream()
                .filter(e -> !e.id().equalsIgnoreCase(event.id()))
                .limit(3)
                .collect(Collectors.toList());

        return new LiveEventDetailResponse(true, finalizedEvent, tiers, nearby);
    }

    public boolean toggleReminder(String id) {
        if (remindedEventIds.contains(id)) {
            remindedEventIds.remove(id);
            return false;
        } else {
            remindedEventIds.add(id);
            return true;
        }
    }

    private LiveEventDto withReminder(LiveEventDto e, boolean isReminded) {
        return new LiveEventDto(
                e.id(), e.title(), e.artistName(), e.artistImageUrl(), e.bannerUrl(),
                e.venue(), e.city(), e.date(), e.time(), e.priceStarting(), e.status(),
                e.category(), e.bookingUrl(), isReminded, e.lineup(), e.setlist()
        );
    }
}