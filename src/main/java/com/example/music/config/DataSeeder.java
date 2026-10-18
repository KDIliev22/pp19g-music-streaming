package com.example.music.config;

import com.example.music.model.AppUser;
import com.example.music.model.Event;
import com.example.music.model.Member;
import com.example.music.model.Playlist;
import com.example.music.model.Role;
import com.example.music.model.TicketOrder;
import com.example.music.model.Track;
import com.example.music.repository.EventRepository;
import com.example.music.repository.MemberRepository;
import com.example.music.repository.PlaylistRepository;
import com.example.music.repository.TicketOrderRepository;
import com.example.music.repository.TrackRepository;
import com.example.music.repository.UserRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Demo data so every screen has something to show: users, tracks, playlists, members, events and ticket orders. */
@Component
public class DataSeeder implements ApplicationRunner {
    private final TrackRepository tracks;
    private final UserRepository users;
    private final PlaylistRepository playlists;
    private final EventRepository events;
    private final MemberRepository members;
    private final TicketOrderRepository orders;
    private final PasswordEncoder encoder;
    private final Path media;
    private final Random rnd = new Random(42);

    public DataSeeder(TrackRepository tracks, UserRepository users, PlaylistRepository playlists, EventRepository events,
                      MemberRepository members, TicketOrderRepository orders, PasswordEncoder encoder,
                      @Value("${app.media-dir}") Path media) {
        this.tracks = tracks; this.users = users; this.playlists = playlists; this.events = events;
        this.members = members; this.orders = orders; this.encoder = encoder; this.media = media;
    }

    @Override public void run(ApplicationArguments args) throws IOException {
        Files.createDirectories(media);
        List<Track> trackList = seedTracks();
        if (users.count() == 0) seedUsers(trackList);
        seedMembers();
        seedEventsAndOrders();
    }

    private List<Track> seedTracks() throws IOException {
        if (tracks.count() > 0) return tracks.findAll();
        Object[][] seed = {
            {"Sine Sunrise", "Test Tones", "Demo EP", 440.0}, {"Mellow Fifth", "Test Tones", "Demo EP", 330.0}, {"High Noon", "Test Tones", "Demo EP", 660.0},
            {"Low Tide", "Sine Wave Society", "Ocean Hz", 220.0}, {"Deep Current", "Sine Wave Society", "Ocean Hz", 247.0}, {"Surface Light", "Sine Wave Society", "Ocean Hz", 392.0},
            {"Night Arp", "Arpeggio Club", "After Dark", 294.0}, {"Neon Steps", "Arpeggio Club", "After Dark", 523.0}};
        List<Track> saved = new ArrayList<>();
        for (Object[] s : seed) {
            String file = ((String) s[0]).replace(' ', '_') + ".wav";
            writeArpeggio(media.resolve(file), (double) s[3]);
            saved.add(tracks.save(new Track(null, (String) s[0], (String) s[1], (String) s[2], file)));
        }
        return saved;
    }

    private void seedUsers(List<Track> t) {
        AppUser admin = users.save(new AppUser("admin", encoder.encode("admin123"), "Системен администратор", "admin@clinic.local", "+359 888 000 001", Role.ADMIN));
        AppUser vet = users.save(new AppUser("vet", encoder.encode("vet12345"), "Д-р Иван Петров", "vet@clinic.local", "+359 888 000 002", Role.VET));
        AppUser reception = users.save(new AppUser("reception", encoder.encode("recep123"), "Мария Георгиева", "reception@clinic.local", "+359 888 000 003", Role.RECEPTION));
        // overlapping playlists give the recommender something to work with
        playlists.save(new Playlist(null, "Chill", admin, pick(t, "Sine Sunrise", "Low Tide", "Deep Current")));
        playlists.save(new Playlist(null, "Focus", vet, pick(t, "Sine Sunrise", "Mellow Fifth", "Low Tide")));
        playlists.save(new Playlist(null, "Night", vet, pick(t, "Night Arp", "Neon Steps", "High Noon")));
        playlists.save(new Playlist(null, "Waiting Room", reception, pick(t, "Mellow Fifth", "Low Tide", "Surface Light")));
    }

    private static Set<Track> pick(List<Track> all, String... titles) {
        Set<String> wanted = Set.of(titles);
        Set<Track> out = new HashSet<>();
        all.stream().filter(x -> wanted.contains(x.getTitle())).forEach(out::add);
        return out;
    }

    private void seedMembers() {
        if (members.count() > 0) return;
        String[] mf = {"Георги", "Иван", "Димитър", "Николай", "Петър"}, ml = {"Иванов", "Георгиев", "Стоянов", "Петков", "Тодоров"};
        String[] ff = {"Мария", "Елена", "Стефани", "Десислава", "Виктория"}, fl = {"Петрова", "Димитрова", "Николова", "Георгиева", "Тодорова"};
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 28; i++) {
            int k = i / 2, last = (k * 3 + k / 5) % 5;
            String name = i % 2 == 0 ? mf[k % 5] + " " + ml[last] : ff[k % 5] + " " + fl[last];
            members.save(new Member(name, "member" + (i + 1) + "@example.com",
                    String.format("+359 89%d %03d %03d", i % 10, 100 + i * 7, 200 + i * 13), i % 9 != 8,
                    now.minusDays((long) (i * i * 0.5)).minusHours(i % 7))); // registrations spread over the last year
        }
    }

    /** Events over the last 12 months; every event's tickets are split into real orders so reports and orders agree. */
    private void seedEventsAndOrders() {
        if (events.count() > 0) return;
        List<Member> buyers = members.findAll().stream().filter(Member::isActive).toList();
        String[] names = {"Open Mic Night", "Jazz in the Park", "Indie Showcase", "DJ Sunset Set", "Acoustic Evening", "Vinyl Market Party"};
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            if (i % 5 == 4) continue; // a couple of quiet months, to show empty states
            YearMonth ym = YearMonth.from(today).minusMonths(i);
            for (int day : new int[]{6, 20}) {
                LocalDate d = ym.atDay(Math.min(day, ym.lengthOfMonth()));
                if (d.isAfter(today)) continue;
                Event ev = events.save(new Event(null, names[rnd.nextInt(names.length)], d,
                        BigDecimal.valueOf(10 + rnd.nextInt(26)), 30 + rnd.nextInt(171)));
                List<TicketOrder> batch = new ArrayList<>();
                for (int left = ev.getTicketsSold(); left > 0; ) {
                    int q = Math.min(left, 1 + rnd.nextInt(6));
                    left -= q;
                    LocalDateTime at = d.minusDays(rnd.nextInt(14)).atTime(8 + rnd.nextInt(12), rnd.nextInt(60));
                    batch.add(new TicketOrder(ev, buyers.get(rnd.nextInt(buyers.size())), q, ev.getTicketPrice().multiply(BigDecimal.valueOf(q)), at));
                }
                orders.saveAll(batch);
            }
        }
    }

    private static void writeArpeggio(Path path, double hz) throws IOException {
        int rate = 22050, n = rate * 24; double[] ratio = {1, 1.25, 1.5, 1.25}; double phase = 0;
        ByteBuffer b = ByteBuffer.allocate(44 + n * 2).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes()).putInt(36 + n * 2).put("WAVEfmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 1)
         .putInt(rate).putInt(rate * 2).putShort((short) 2).putShort((short) 16).put("data".getBytes()).putInt(n * 2);
        for (int i = 0; i < n; i++) {
            phase += 2 * Math.PI * hz * ratio[(i / (rate / 2)) % 4] / rate;
            double fade = 1 - (i % (rate / 2)) / (double) (rate / 2) * 0.6; // plucked feel
            b.putShort((short) (Math.sin(phase) * 7000 * fade));
        }
        Files.write(path, b.array());
    }
}
