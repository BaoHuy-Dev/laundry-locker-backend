package com.huynqb.laundrylockerbackend.core.config;

import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceStatus;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceType;
import com.huynqb.laundrylockerbackend.module.laundry.model.LaundryService;
import com.huynqb.laundrylockerbackend.module.laundry.repository.LaundryServiceRepository;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxSize;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
import com.huynqb.laundrylockerbackend.module.locker.enums.LockerStatus;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.locker.model.Locker;
import com.huynqb.laundrylockerbackend.module.locker.repository.BoxRepository;
import com.huynqb.laundrylockerbackend.module.locker.repository.LockerRepository;
import com.huynqb.laundrylockerbackend.module.loyalty.enums.PointTransactionType;
import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampTransactionType;
import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampType;
import com.huynqb.laundrylockerbackend.module.loyalty.model.LoyaltyAccount;
import com.huynqb.laundrylockerbackend.module.loyalty.model.PointTransaction;
import com.huynqb.laundrylockerbackend.module.loyalty.model.StampCard;
import com.huynqb.laundrylockerbackend.module.loyalty.model.StampTransaction;
import com.huynqb.laundrylockerbackend.module.loyalty.repository.LoyaltyAccountRepository;
import com.huynqb.laundrylockerbackend.module.loyalty.repository.PointTransactionRepository;
import com.huynqb.laundrylockerbackend.module.loyalty.repository.StampCardRepository;
import com.huynqb.laundrylockerbackend.module.loyalty.repository.StampTransactionRepository;
import com.huynqb.laundrylockerbackend.module.notification.enums.NotificationStatus;
import com.huynqb.laundrylockerbackend.module.notification.enums.NotificationType;
import com.huynqb.laundrylockerbackend.module.notification.model.Notification;
import com.huynqb.laundrylockerbackend.module.notification.repository.NotificationRepository;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderType;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.model.OrderDetail;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderDetailRepository;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeStatus;
import com.huynqb.laundrylockerbackend.module.partner.enums.PartnerStatus;
import com.huynqb.laundrylockerbackend.module.partner.model.Partner;
import com.huynqb.laundrylockerbackend.module.partner.model.StaffAccessCode;
import com.huynqb.laundrylockerbackend.module.partner.repository.PartnerRepository;
import com.huynqb.laundrylockerbackend.module.partner.repository.StaffAccessCodeRepository;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentMethod;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentStatus;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import com.huynqb.laundrylockerbackend.module.payment.repository.PaymentRepository;
import com.huynqb.laundrylockerbackend.module.store.enums.StoreStatus;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprehensive sample data loader for dev and docker profiles. Creates realistic data for all
 * tables covering multiple scenarios.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SampleDataLoader {

  // Repositories
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PartnerRepository partnerRepository;
  private final StoreRepository storeRepository;
  private final LockerRepository lockerRepository;
  private final BoxRepository boxRepository;
  private final LaundryServiceRepository serviceRepository;
  private final OrderRepository orderRepository;
  private final OrderDetailRepository orderDetailRepository;
  private final StaffAccessCodeRepository accessCodeRepository;
  private final PaymentRepository paymentRepository;
  private final NotificationRepository notificationRepository;
  private final LoyaltyAccountRepository loyaltyAccountRepository;
  private final PointTransactionRepository pointTransactionRepository;
  private final StampCardRepository stampCardRepository;
  private final StampTransactionRepository stampTransactionRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.data.load-sample-data:false}")
  private boolean loadSampleData;

  @Value("${app.data.force-reload:false}")
  private boolean forceReload;

  private static final String DEFAULT_PASSWORD = "password123";

  // Cached entities for reference
  private List<User> allUsers;
  private List<Partner> allPartners;
  private List<Store> allStores;
  private List<Locker> allLockers;
  private List<LaundryService> allServices;
  private List<Order> allOrders;

  @Bean
  @Profile({"dev", "docker"})
  public CommandLineRunner loadSampleData() {
    return args -> {
      log.info("========================================");
      log.info("📦 SAMPLE DATA LOADER");
      log.info("========================================");
      log.info("   load-sample-data: {}", loadSampleData);
      log.info("   force-reload: {}", forceReload);

      if (!loadSampleData) {
        log.info("   Status: DISABLED");
        log.info("========================================");
        return;
      }

      long userCount = userRepository.count();
      log.info("   Existing users: {}", userCount);

      if (userCount > 0 && !forceReload) {
        log.info("   Status: SKIPPED (data exists)");
        log.info("   Set app.data.force-reload=true to reload.");
        log.info("========================================");
        return;
      }

      if (forceReload && userCount > 0) {
        log.info("   Force reload enabled...");
        clearAllData();
      }

      log.info("🚀 Loading comprehensive sample data...");
      loadAllData();
      log.info("✅ Sample data loaded successfully!");
      printSummary();
      log.info("========================================");
    };
  }

  @Transactional
  public void clearAllData() {
    log.info("   Clearing existing data...");
    stampTransactionRepository.deleteAll();
    stampCardRepository.deleteAll();
    pointTransactionRepository.deleteAll();
    loyaltyAccountRepository.deleteAll();
    notificationRepository.deleteAll();
    paymentRepository.deleteAll();
    accessCodeRepository.deleteAll();
    orderDetailRepository.deleteAll();
    orderRepository.deleteAll();
    boxRepository.deleteAll();
    lockerRepository.deleteAll();
    serviceRepository.deleteAll();
    storeRepository.deleteAll();
    partnerRepository.deleteAll();
    userRepository.deleteAll();
    log.info("   ✓ All data cleared");
  }

  @Transactional
  public void loadAllData() {
    // 1. Roles
    List<Role> roles = createRoles();
    log.info("   ✓ {} roles", roles.size());

    // 2. Users (15 users)
    allUsers = createUsers(roles);
    log.info("   ✓ {} users", allUsers.size());

    // 3. Partners (3 partners)
    allPartners = createPartners();
    log.info("   ✓ {} partners", allPartners.size());

    // 4. Stores (5 stores)
    allStores = createStores();
    log.info("   ✓ {} stores", allStores.size());

    // 5. Lockers & Boxes (8 lockers, ~50 boxes)
    allLockers = createLockers();
    log.info("   ✓ {} lockers with boxes", allLockers.size());

    // 6. Services (10 services per store)
    allServices = createServices();
    log.info("   ✓ {} services", allServices.size());

    // 7. Orders (20 orders with various statuses)
    allOrders = createOrders();
    log.info("   ✓ {} orders", allOrders.size());

    // 8. Payments
    int payments = createPayments();
    log.info("   ✓ {} payments", payments);

    // 9. Staff Access Codes
    int codes = createAccessCodes();
    log.info("   ✓ {} access codes", codes);

    // 10. Loyalty Accounts
    int loyaltyAccounts = createLoyaltyAccounts();
    log.info("   ✓ {} loyalty accounts", loyaltyAccounts);

    // 11. Point Transactions
    int pointTxns = createPointTransactions();
    log.info("   ✓ {} point transactions", pointTxns);

    // 12. Stamp Cards
    int stampCards = createStampCards();
    log.info("   ✓ {} stamp cards", stampCards);

    // 13. Notifications
    int notifications = createNotifications();
    log.info("   ✓ {} notifications", notifications);
  }

  // ==================== ROLES ====================
  private List<Role> createRoles() {
    return List.of(
        saveRole(RoleName.ADMIN),
        saveRole(RoleName.USER),
        saveRole(RoleName.PARTNER),
        saveRole(RoleName.STAFF),
        saveRole(RoleName.MODERATOR));
  }

  private Role saveRole(RoleName name) {
    return roleRepository
        .findByName(name)
        .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
  }

  // ==================== USERS ====================
  private List<User> createUsers(List<Role> roles) {
    Role adminRole = findRole(roles, RoleName.ADMIN);
    Role userRole = findRole(roles, RoleName.USER);
    Role partnerRole = findRole(roles, RoleName.PARTNER);
    Role staffRole = findRole(roles, RoleName.STAFF);

    String pwd = passwordEncoder.encode(DEFAULT_PASSWORD);

    List<User> users = new ArrayList<>();

    // Admin
    users.add(
        createUser(
            "admin@laundrylocker.com", "System", "Admin", "0900000001", pwd, Set.of(adminRole)));

    // Partners (3)
    users.add(
        createUser(
            "partner.minh@gmail.com",
            "Minh",
            "Nguyễn Văn",
            "0900000002",
            pwd,
            Set.of(userRole, partnerRole)));
    users.add(
        createUser(
            "partner.huong@gmail.com",
            "Hương",
            "Trần Thị",
            "0900000003",
            pwd,
            Set.of(userRole, partnerRole)));
    users.add(
        createUser(
            "partner.nam@gmail.com",
            "Nam",
            "Lê Hoàng",
            "0900000004",
            pwd,
            Set.of(userRole, partnerRole)));

    // Customers (10)
    users.add(
        createUser("customer.huy@gmail.com", "Huy", "Lê Văn", "0901000001", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.lan@gmail.com", "Lan", "Phạm Thị", "0901000002", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.tuan@gmail.com", "Tuấn", "Võ Minh", "0901000003", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.mai@gmail.com", "Mai", "Nguyễn Thị", "0901000004", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.duc@gmail.com", "Đức", "Trần Văn", "0901000005", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.linh@gmail.com", "Linh", "Hoàng Thị", "0901000006", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.khoa@gmail.com", "Khoa", "Đặng Văn", "0901000007", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.hang@gmail.com", "Hằng", "Bùi Thị", "0901000008", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.long@gmail.com", "Long", "Phạm Hoàng", "0901000009", pwd, Set.of(userRole)));
    users.add(
        createUser("customer.thu@gmail.com", "Thu", "Lý Thị", "0901000010", pwd, Set.of(userRole)));

    // Staff-like user (for testing)
    users.add(
        createUser(
            "staff.tung@gmail.com",
            "Tùng",
            "Nguyễn Văn",
            "0902000001",
            pwd,
            Set.of(userRole, staffRole)));

    return users;
  }

  private User createUser(
      String email, String firstName, String lastName, String phone, String pwd, Set<Role> roles) {
    return userRepository.save(
        User.builder()
            .email(email)
            .name(firstName + " " + lastName)
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(phone)
            .password(pwd)
            .provider(AuthProvider.LOCAL)
            .emailVerified(true)
            .roles(new HashSet<>(roles))
            .build());
  }

  // ==================== PARTNERS ====================
  private List<Partner> createPartners() {
    User admin = findUserByEmail("admin@laundrylocker.com");
    User minh = findUserByEmail("partner.minh@gmail.com");
    User huong = findUserByEmail("partner.huong@gmail.com");
    User nam = findUserByEmail("partner.nam@gmail.com");

    return List.of(
        partnerRepository.save(
            Partner.builder()
                .user(minh)
                .businessName("Giặt Ủi Sạch Sẽ - Quận 1")
                .businessRegistrationNumber("BRN-Q1-001")
                .taxId("TAX-Q1-001")
                .businessAddress("123 Nguyễn Huệ, Quận 1, TP.HCM")
                .contactPhone("0900000002")
                .contactEmail("partner.minh@gmail.com")
                .status(PartnerStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusMonths(6))
                .approvedBy(admin.getId())
                .revenueSharePercent(new BigDecimal("70.00"))
                .notes("Đối tác chiến lược khu vực trung tâm")
                .build()),
        partnerRepository.save(
            Partner.builder()
                .user(huong)
                .businessName("Tiệm Giặt Hương Sắc - Quận 3")
                .businessRegistrationNumber("BRN-Q3-001")
                .taxId("TAX-Q3-001")
                .businessAddress("456 Võ Văn Tần, Quận 3, TP.HCM")
                .contactPhone("0900000003")
                .contactEmail("partner.huong@gmail.com")
                .status(PartnerStatus.APPROVED)
                .approvedAt(LocalDateTime.now().minusMonths(3))
                .approvedBy(admin.getId())
                .revenueSharePercent(new BigDecimal("65.00"))
                .build()),
        partnerRepository.save(
            Partner.builder()
                .user(nam)
                .businessName("Clean & Fresh Laundry - Quận 7")
                .businessRegistrationNumber("BRN-Q7-001")
                .taxId("TAX-Q7-001")
                .businessAddress("789 Nguyễn Văn Linh, Quận 7, TP.HCM")
                .contactPhone("0900000004")
                .contactEmail("partner.nam@gmail.com")
                .status(PartnerStatus.PENDING) // Chưa duyệt
                .revenueSharePercent(new BigDecimal("70.00"))
                .notes("Đang chờ xác minh giấy tờ")
                .build()));
  }

  // ==================== STORES ====================
  private List<Store> createStores() {
    Partner p1 = allPartners.get(0); // Minh
    Partner p2 = allPartners.get(1); // Hương

    return List.of(
        // Partner 1 stores (3)
        storeRepository.save(
            Store.builder()
                .name("Cửa hàng Q1 - Nguyễn Huệ")
                .address("123 Nguyễn Huệ, Quận 1, TP.HCM")
                .contactPhone("0281234567")
                .latitude(10.7769)
                .longitude(106.7009)
                .status(StoreStatus.ACTIVE)
                .description("Cửa hàng flagship tại trung tâm Q1")
                .partner(p1)
                .build()),
        storeRepository.save(
            Store.builder()
                .name("Cửa hàng Q1 - Lê Lợi")
                .address("789 Lê Lợi, Quận 1, TP.HCM")
                .contactPhone("0281234568")
                .latitude(10.7731)
                .longitude(106.7012)
                .status(StoreStatus.ACTIVE)
                .partner(p1)
                .build()),
        storeRepository.save(
            Store.builder()
                .name("Cửa hàng Q1 - Pasteur")
                .address("100 Pasteur, Quận 1, TP.HCM")
                .contactPhone("0281234569")
                .latitude(10.7800)
                .longitude(106.6950)
                .status(StoreStatus.INACTIVE) // Đang bảo trì
                .description("Tạm đóng cửa để nâng cấp")
                .partner(p1)
                .build()),

        // Partner 2 stores (2)
        storeRepository.save(
            Store.builder()
                .name("Cửa hàng Q3 - Võ Văn Tần")
                .address("456 Võ Văn Tần, Quận 3, TP.HCM")
                .contactPhone("0282345678")
                .latitude(10.7756)
                .longitude(106.6863)
                .status(StoreStatus.ACTIVE)
                .partner(p2)
                .build()),
        storeRepository.save(
            Store.builder()
                .name("Cửa hàng Q3 - Nguyễn Đình Chiểu")
                .address("200 Nguyễn Đình Chiểu, Quận 3, TP.HCM")
                .contactPhone("0282345679")
                .latitude(10.7780)
                .longitude(106.6900)
                .status(StoreStatus.ACTIVE)
                .partner(p2)
                .build()));
  }

  // ==================== LOCKERS & BOXES ====================
  private List<Locker> createLockers() {
    List<Locker> lockers = new ArrayList<>();

    // Store 1: 2 lockers
    Store s1 = allStores.get(0);
    lockers.add(
        createLockerWithBoxes(
            "LOC-Q1-NH-01",
            "Tủ A - Nguyễn Huệ",
            s1,
            LockerStatus.ACTIVE,
            new BoxSize[] {
              BoxSize.SMALL,
              BoxSize.SMALL,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.LARGE,
              BoxSize.LARGE,
              BoxSize.EXTRA_LARGE
            }));
    lockers.add(
        createLockerWithBoxes(
            "LOC-Q1-NH-02",
            "Tủ B - Nguyễn Huệ",
            s1,
            LockerStatus.ACTIVE,
            new BoxSize[] {
              BoxSize.SMALL,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.LARGE,
              BoxSize.LARGE,
              BoxSize.EXTRA_LARGE
            }));

    // Store 2: 2 lockers
    Store s2 = allStores.get(1);
    lockers.add(
        createLockerWithBoxes(
            "LOC-Q1-LL-01",
            "Tủ A - Lê Lợi",
            s2,
            LockerStatus.ACTIVE,
            new BoxSize[] {
              BoxSize.SMALL,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.LARGE,
              BoxSize.LARGE,
              BoxSize.EXTRA_LARGE
            }));
    lockers.add(
        createLockerWithBoxes(
            "LOC-Q1-LL-02",
            "Tủ B - Lê Lợi",
            s2,
            LockerStatus.MAINTENANCE,
            new BoxSize[] {BoxSize.SMALL, BoxSize.MEDIUM, BoxSize.LARGE, BoxSize.EXTRA_LARGE}));

    // Store 4: 2 lockers
    Store s4 = allStores.get(3);
    lockers.add(
        createLockerWithBoxes(
            "LOC-Q3-VVT-01",
            "Tủ A - Võ Văn Tần",
            s4,
            LockerStatus.ACTIVE,
            new BoxSize[] {
              BoxSize.SMALL,
              BoxSize.SMALL,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.LARGE,
              BoxSize.LARGE
            }));
    lockers.add(
        createLockerWithBoxes(
            "LOC-Q3-VVT-02",
            "Tủ B - Võ Văn Tần",
            s4,
            LockerStatus.ACTIVE,
            new BoxSize[] {
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.LARGE,
              BoxSize.LARGE,
              BoxSize.EXTRA_LARGE,
              BoxSize.EXTRA_LARGE
            }));

    // Store 5: 2 lockers
    Store s5 = allStores.get(4);
    lockers.add(
        createLockerWithBoxes(
            "LOC-Q3-NDC-01",
            "Tủ A - Nguyễn Đình Chiểu",
            s5,
            LockerStatus.ACTIVE,
            new BoxSize[] {
              BoxSize.SMALL, BoxSize.MEDIUM, BoxSize.MEDIUM, BoxSize.LARGE, BoxSize.EXTRA_LARGE
            }));
    lockers.add(
        createLockerWithBoxes(
            "LOC-Q3-NDC-02",
            "Tủ B - Nguyễn Đình Chiểu",
            s5,
            LockerStatus.DISCONNECTED,
            new BoxSize[] {BoxSize.MEDIUM, BoxSize.LARGE, BoxSize.LARGE, BoxSize.EXTRA_LARGE}));

    return lockers;
  }

  private Locker createLockerWithBoxes(
      String code, String name, Store store, LockerStatus status, BoxSize[] sizes) {
    Locker locker =
        lockerRepository.save(
            Locker.builder().code(code).name(name).store(store).status(status).build());

    for (int i = 0; i < sizes.length; i++) {
      boxRepository.save(
          Box.builder()
              .locker(locker)
              .boxNumber(i + 1)
              .size(sizes[i])
              .status(BoxStatus.AVAILABLE)
              .isActive(true)
              .build());
    }
    return locker;
  }

  // ==================== SERVICES ====================
  private List<LaundryService> createServices() {
    List<LaundryService> services = new ArrayList<>();
    Store mainStore = allStores.get(0);

    // Storage services
    services.add(
        saveService(
            "Gửi hàng thường",
            "Gửi đồ vào tủ với phí cơ bản",
            5000,
            null,
            "lần",
            ServiceType.STANDARD_DROPOFF,
            false,
            false,
            0,
            mainStore));
    services.add(
        saveService(
            "Gửi qua đêm",
            "Phí phụ thu khi gửi qua đêm",
            5000,
            null,
            "đêm",
            ServiceType.OVERNIGHT,
            true,
            false,
            0,
            mainStore));
    services.add(
        saveService(
            "Gửi nhanh 2h",
            "Lấy đồ trong vòng 2 tiếng",
            10000,
            null,
            "lần",
            ServiceType.EXPRESS_2H,
            false,
            false,
            2,
            mainStore));

    // Laundry services
    services.add(
        saveService(
            "Giặt sấy thường",
            "Giặt và sấy khô quần áo thông thường",
            12000,
            15000,
            "kg",
            ServiceType.LAUNDRY,
            false,
            false,
            24,
            mainStore));
    services.add(
        saveService(
            "Giặt hấp cao cấp",
            "Giặt hấp cho đồ cao cấp, vest, áo dài",
            25000,
            35000,
            "món",
            ServiceType.LAUNDRY,
            false,
            false,
            48,
            mainStore));
    services.add(
        saveService(
            "Giặt đồ lớn",
            "Chăn, mền, rèm cửa",
            30000,
            50000,
            "món",
            ServiceType.LAUNDRY,
            false,
            false,
            48,
            mainStore));
    services.add(
        saveService(
            "Giặt giày/túi",
            "Giặt giày, túi xách",
            40000,
            80000,
            "đôi/cái",
            ServiceType.LAUNDRY,
            false,
            false,
            72,
            mainStore));
    services.add(
        saveService(
            "Là ủi",
            "Chỉ là ủi, không giặt",
            8000,
            12000,
            "món",
            ServiceType.LAUNDRY,
            false,
            false,
            4,
            mainStore));

    // Monthly packages
    services.add(
        saveService(
            "Gói tháng Sinh viên",
            "Giặt không giới hạn cho sinh viên",
            50000,
            null,
            "tháng",
            ServiceType.MONTHLY_STUDENT,
            false,
            true,
            0,
            mainStore));
    services.add(
        saveService(
            "Gói tháng Shipper",
            "Giặt không giới hạn cho shipper",
            70000,
            null,
            "tháng",
            ServiceType.MONTHLY_SHIPPER,
            false,
            true,
            0,
            mainStore));

    return services;
  }

  private LaundryService saveService(
      String name,
      String desc,
      int price,
      Integer maxPrice,
      String unit,
      ServiceType type,
      boolean isAddon,
      boolean isMonthly,
      int hours,
      Store store) {
    return serviceRepository.save(
        LaundryService.builder()
            .name(name)
            .description(desc)
            .price(new BigDecimal(price))
            .maxPrice(maxPrice != null ? new BigDecimal(maxPrice) : null)
            .unit(unit)
            .serviceType(type)
            .status(ServiceStatus.ACTIVE)
            .isAddon(isAddon)
            .isMonthlyPackage(isMonthly)
            .estimatedHours(hours)
            .store(store)
            .build());
  }

  // ==================== ORDERS ====================
  private List<Order> createOrders() {
    List<Order> orders = new ArrayList<>();
    List<Box> boxes = boxRepository.findByLockerId(allLockers.get(0).getId());

    User huy = findUserByEmail("customer.huy@gmail.com");
    User lan = findUserByEmail("customer.lan@gmail.com");
    User tuan = findUserByEmail("customer.tuan@gmail.com");
    User mai = findUserByEmail("customer.mai@gmail.com");
    User duc = findUserByEmail("customer.duc@gmail.com");
    User linh = findUserByEmail("customer.linh@gmail.com");
    User khoa = findUserByEmail("customer.khoa@gmail.com");
    User hang = findUserByEmail("customer.hang@gmail.com");
    User longUser = findUserByEmail("customer.long@gmail.com");
    User thu = findUserByEmail("customer.thu@gmail.com");

    Locker locker1 = allLockers.get(0);
    Locker locker2 = allLockers.get(2);
    Locker locker3 = allLockers.get(4);

    LaundryService storage = allServices.get(0);
    LaundryService laundry = allServices.get(3);
    LaundryService premium = allServices.get(4);
    LaundryService large = allServices.get(5);
    LaundryService express = allServices.get(2);

    // ===== COMPLETED Orders (6) =====
    orders.add(
        createFullOrder(
            huy,
            locker1,
            null,
            null,
            OrderStatus.COMPLETED,
            null,
            2.5,
            42500,
            "Giặt quần áo hàng ngày",
            -7,
            -5,
            storage,
            laundry));
    orders.add(
        createFullOrder(
            lan,
            locker1,
            null,
            null,
            OrderStatus.COMPLETED,
            null,
            1.8,
            32000,
            "Giặt đồ công sở",
            -14,
            -12,
            storage,
            laundry));
    orders.add(
        createFullOrder(
            tuan,
            locker2,
            null,
            null,
            OrderStatus.COMPLETED,
            null,
            4.0,
            75000,
            "Giặt đồ gia đình",
            -21,
            -19,
            storage,
            laundry));
    orders.add(
        createFullOrder(
            linh,
            locker1,
            null,
            null,
            OrderStatus.COMPLETED,
            null,
            1.0,
            30000,
            "Giặt áo vest",
            -10,
            -8,
            storage,
            premium));
    orders.add(
        createFullOrder(
            khoa,
            locker3,
            null,
            null,
            OrderStatus.COMPLETED,
            null,
            3.2,
            58000,
            "Giặt chăn mền",
            -5,
            -3,
            storage,
            large));
    orders.add(
        createFullOrder(
            hang,
            locker2,
            null,
            null,
            OrderStatus.COMPLETED,
            null,
            2.0,
            39000,
            "Giặt thường",
            -3,
            -1,
            storage,
            laundry));

    // ===== CANCELED Orders (2) =====
    orders.add(
        createFullOrder(
            thu,
            locker1,
            null,
            null,
            OrderStatus.CANCELED,
            null,
            null,
            0,
            "Đơn hủy do khách không đến",
            -2,
            null,
            storage,
            laundry));
    orders.add(
        createFullOrder(
            longUser,
            locker2,
            null,
            null,
            OrderStatus.CANCELED,
            null,
            null,
            0,
            "Khách yêu cầu hủy",
            -4,
            null,
            storage,
            premium));

    // ===== WAITING Orders (3) =====
    Box box1 = boxes.get(0);
    box1.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(box1);
    orders.add(
        createFullOrder(
            lan,
            locker1,
            box1,
            null,
            OrderStatus.WAITING,
            "123456",
            null,
            65000,
            "Giặt áo vest và quần tây",
            0,
            null,
            storage,
            premium));

    Box box2 = boxes.get(1);
    box2.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(box2);
    orders.add(
        createFullOrder(
            duc,
            locker1,
            box2,
            null,
            OrderStatus.WAITING,
            "234567",
            null,
            45000,
            "Giặt đồ thể thao",
            0,
            null,
            storage,
            laundry));

    List<Box> boxes3 = boxRepository.findByLockerId(allLockers.get(4).getId());
    Box box3 = boxes3.get(0);
    box3.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(box3);
    orders.add(
        createFullOrder(
            linh,
            locker3,
            box3,
            null,
            OrderStatus.WAITING,
            "345678",
            null,
            80000,
            "Giặt chăn",
            0,
            null,
            storage,
            large));

    // ===== COLLECTED Orders (2) =====
    orders.add(
        createFullOrder(
            khoa,
            locker1,
            null,
            null,
            OrderStatus.COLLECTED,
            null,
            2.5,
            47000,
            "Đã lấy, đang cân",
            0,
            null,
            storage,
            laundry));
    orders.add(
        createFullOrder(
            hang,
            locker2,
            null,
            null,
            OrderStatus.COLLECTED,
            null,
            1.5,
            35000,
            "Đang xử lý tại xưởng",
            0,
            null,
            storage,
            laundry));

    // ===== PROCESSING Orders (2) =====
    orders.add(
        createFullOrder(
            tuan,
            locker1,
            null,
            null,
            OrderStatus.PROCESSING,
            null,
            3.0,
            55000,
            "Đang giặt chăn mền",
            -1,
            null,
            storage,
            large));
    orders.add(
        createFullOrder(
            mai,
            locker3,
            null,
            null,
            OrderStatus.PROCESSING,
            null,
            2.2,
            41000,
            "Đang giặt",
            -1,
            null,
            storage,
            laundry));

    // ===== READY Orders (2) =====
    orders.add(
        createFullOrder(
            huy,
            locker1,
            null,
            null,
            OrderStatus.READY,
            null,
            1.8,
            33000,
            "Đã giặt xong, chờ trả",
            -1,
            null,
            storage,
            laundry));
    orders.add(
        createFullOrder(
            longUser,
            locker2,
            null,
            null,
            OrderStatus.READY,
            null,
            2.5,
            48000,
            "Sẵn sàng trả về tủ",
            -1,
            null,
            storage,
            laundry));

    // ===== RETURNED Orders (2) =====
    Box box4 = boxes.get(3);
    box4.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(box4);
    orders.add(
        createFullOrder(
            mai,
            locker1,
            null,
            box4,
            OrderStatus.RETURNED,
            "654321",
            1.5,
            32500,
            "Đồ đã về tủ, chờ lấy",
            -1,
            null,
            storage,
            laundry));

    Box box5 = boxes.get(4);
    box5.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(box5);
    orders.add(
        createFullOrder(
            thu,
            locker1,
            null,
            box5,
            OrderStatus.RETURNED,
            "765432",
            2.0,
            39000,
            "Chờ khách thanh toán",
            -1,
            null,
            storage,
            laundry));

    // ===== INITIALIZED Order (1) =====
    List<Box> boxes2 = boxRepository.findByLockerId(allLockers.get(1).getId());
    Box box6 = boxes2.get(0);
    box6.setStatus(BoxStatus.RESERVED);
    boxRepository.save(box6);
    orders.add(
        createFullOrder(
            duc,
            allLockers.get(1),
            box6,
            null,
            OrderStatus.INITIALIZED,
            "111222",
            null,
            17000,
            "Mới tạo, chưa bỏ đồ",
            0,
            null,
            storage,
            laundry));

    return orders;
  }

  private Order createFullOrder(
      User sender,
      Locker locker,
      Box sendBox,
      Box receiveBox,
      OrderStatus status,
      String pin,
      Double weight,
      int totalPrice,
      String desc,
      int createdDaysAgo,
      Integer completedDaysAgo,
      LaundryService... services) {
    Order order =
        orderRepository.save(
            Order.builder()
                .type(OrderType.LAUNDRY)
                .sender(sender)
                .locker(locker)
                .sendBox(sendBox)
                .receiveBox(receiveBox)
                .status(status)
                .pinCode(pin)
                .pinCodeIssuedAt(pin != null ? LocalDateTime.now() : null)
                .actualWeight(weight != null ? new BigDecimal(weight) : null)
                .weightUnit(weight != null ? "kg" : null)
                .reservationFee(new BigDecimal("5000"))
                .storagePrice(new BigDecimal("5000"))
                .totalPrice(new BigDecimal(totalPrice))
                .description(desc)
                .completedAt(
                    completedDaysAgo != null
                        ? LocalDateTime.now().minusDays(-completedDaysAgo)
                        : null)
                .build());

    order.setCreatedAt(LocalDateTime.now().minusDays(-createdDaysAgo));
    orderRepository.save(order);

    for (int i = 0; i < services.length; i++) {
      orderDetailRepository.save(
          OrderDetail.builder()
              .order(order)
              .service(services[i])
              .quantity(i == 0 ? 1.0 : (weight != null ? weight : 1.0))
              .price(services[i].getPrice())
              .build());
    }

    return order;
  }

  // ==================== PAYMENTS ====================
  private int createPayments() {
    List<Order> completedOrders =
        allOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).toList();

    int count = 0;
    for (Order order : completedOrders) {
      paymentRepository.save(
          Payment.builder()
              .order(order)
              .customer(order.getSender())
              .amount(order.getTotalPrice())
              .method(count % 2 == 0 ? PaymentMethod.VNPAY : PaymentMethod.MOMO)
              .status(PaymentStatus.COMPLETED)
              .referenceId("PAY-" + order.getId() + "-" + System.currentTimeMillis())
              .referenceTransactionId("TXN" + (100000 + count))
              .description("Thanh toán đơn hàng #" + order.getId())
              .build());
      count++;
    }

    // Pending payment for RETURNED order
    Order returnedOrder =
        allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.RETURNED)
            .findFirst()
            .orElse(null);
    if (returnedOrder != null) {
      paymentRepository.save(
          Payment.builder()
              .order(returnedOrder)
              .customer(returnedOrder.getSender())
              .amount(returnedOrder.getTotalPrice())
              .method(PaymentMethod.VNPAY)
              .status(PaymentStatus.PENDING)
              .referenceId("PAY-" + returnedOrder.getId() + "-PENDING")
              .url("https://sandbox.vnpayment.vn/paymentv2/...")
              .description("Đang chờ thanh toán")
              .build());
      count++;
    }

    // Failed payment
    paymentRepository.save(
        Payment.builder()
            .order(allOrders.get(0))
            .customer(allOrders.get(0).getSender())
            .amount(new BigDecimal("50000"))
            .method(PaymentMethod.MOMO)
            .status(PaymentStatus.FAILED)
            .referenceId("PAY-FAILED-001")
            .description("Thanh toán thất bại - Timeout")
            .build());
    count++;

    return count;
  }

  // ==================== ACCESS CODES ====================
  private int createAccessCodes() {
    Partner partner1 = allPartners.get(0);
    Partner partner2 = allPartners.get(1);

    List<Order> waitingOrders =
        allOrders.stream().filter(o -> o.getStatus() == OrderStatus.WAITING).toList();

    int count = 0;
    String[] codes = {"ABC12XYZ", "DEF34UVW", "GHI56RST"};
    for (int i = 0; i < Math.min(waitingOrders.size(), codes.length); i++) {
      accessCodeRepository.save(
          StaffAccessCode.builder()
              .code(codes[i])
              .order(waitingOrders.get(i))
              .partner(i % 2 == 0 ? partner1 : partner2)
              .action(AccessCodeAction.COLLECT)
              .status(AccessCodeStatus.ACTIVE)
              .expiresAt(LocalDateTime.now().plusHours(24))
              .notes("Mã cho nhân viên lấy đồ")
              .build());
      count++;
    }

    // Used code
    Order collectedOrder =
        allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.COLLECTED)
            .findFirst()
            .orElse(null);
    if (collectedOrder != null) {
      accessCodeRepository.save(
          StaffAccessCode.builder()
              .code("USED1234")
              .order(collectedOrder)
              .partner(partner1)
              .action(AccessCodeAction.COLLECT)
              .status(AccessCodeStatus.USED)
              .usedAt(LocalDateTime.now().minusHours(2))
              .staffName("Nguyễn Văn Tùng")
              .build());
      count++;
    }

    // Expired code
    accessCodeRepository.save(
        StaffAccessCode.builder()
            .code("EXPIRED1")
            .order(allOrders.get(0))
            .partner(partner1)
            .action(AccessCodeAction.COLLECT)
            .status(AccessCodeStatus.EXPIRED)
            .expiresAt(LocalDateTime.now().minusDays(1))
            .build());
    count++;

    // Return code for READY order
    Order readyOrder =
        allOrders.stream().filter(o -> o.getStatus() == OrderStatus.READY).findFirst().orElse(null);
    if (readyOrder != null) {
      accessCodeRepository.save(
          StaffAccessCode.builder()
              .code("RET12345")
              .order(readyOrder)
              .partner(partner1)
              .action(AccessCodeAction.RETURN)
              .status(AccessCodeStatus.ACTIVE)
              .expiresAt(LocalDateTime.now().plusHours(12))
              .notes("Mã trả đồ về tủ")
              .build());
      count++;
    }

    return count;
  }

  // ==================== LOYALTY ====================
  private int createLoyaltyAccounts() {
    int count = 0;
    long[] points = {500L, 1200L, 200L, 3000L, 0L, 850L, 1500L, 100L, 2200L, 50L};
    BigDecimal[] spent = {
      new BigDecimal("500000"),
      new BigDecimal("1200000"),
      new BigDecimal("200000"),
      new BigDecimal("3000000"),
      BigDecimal.ZERO,
      new BigDecimal("850000"),
      new BigDecimal("1500000"),
      new BigDecimal("100000"),
      new BigDecimal("2200000"),
      new BigDecimal("50000")
    };

    for (int i = 4; i < 14 && i < allUsers.size(); i++) { // Customers only
      User user = allUsers.get(i);
      int idx = i - 4;
      loyaltyAccountRepository.save(
          LoyaltyAccount.builder()
              .user(user)
              .pointsBalance(points[idx])
              .totalPointsEarned(points[idx] + (idx * 100L))
              .totalPointsRedeemed(idx * 100L)
              .totalAmountSpent(spent[idx])
              .build());
      count++;
    }
    return count;
  }

  private int createPointTransactions() {
    int count = 0;
    List<LoyaltyAccount> accounts = loyaltyAccountRepository.findAll();

    for (LoyaltyAccount acc : accounts) {
      // Earn from order
      if (acc.getTotalPointsEarned() > 0) {
        pointTransactionRepository.save(
            PointTransaction.builder()
                .user(acc.getUser())
                .type(PointTransactionType.EARN)
                .points(acc.getTotalPointsEarned())
                .relatedAmount(acc.getTotalAmountSpent())
                .balanceAfter(acc.getPointsBalance())
                .description("Điểm tích lũy từ các đơn hàng")
                .build());
        count++;
      }

      // Redeem
      if (acc.getTotalPointsRedeemed() > 0) {
        pointTransactionRepository.save(
            PointTransaction.builder()
                .user(acc.getUser())
                .type(PointTransactionType.REDEEM)
                .points(-acc.getTotalPointsRedeemed())
                .relatedAmount(new BigDecimal(acc.getTotalPointsRedeemed()))
                .balanceAfter(acc.getPointsBalance())
                .description("Đổi điểm giảm giá")
                .build());
        count++;
      }
    }

    // Bonus points
    if (!accounts.isEmpty()) {
      pointTransactionRepository.save(
          PointTransaction.builder()
              .user(accounts.get(0).getUser())
              .type(PointTransactionType.BONUS)
              .points(100L)
              .balanceAfter(accounts.get(0).getPointsBalance() + 100)
              .description("Thưởng khách hàng mới")
              .build());
      count++;
    }

    return count;
  }

  private int createStampCards() {
    int count = 0;
    List<LoyaltyAccount> accounts = loyaltyAccountRepository.findAll();

    for (int i = 0; i < Math.min(5, accounts.size()); i++) {
      User user = accounts.get(i).getUser();

      // Box stamp card
      StampCard boxCard =
          stampCardRepository.save(
              StampCard.builder()
                  .user(user)
                  .stampType(StampType.BOX)
                  .boxSize("MEDIUM")
                  .stampsRequired(6)
                  .currentStamps(i + 1)
                  .freeRewardsAvailable(i >= 5 ? 1 : 0)
                  .totalStampsEarned(i + 1)
                  .build());
      count++;

      // Transaction
      stampTransactionRepository.save(
          StampTransaction.builder()
              .user(user)
              .stampCard(boxCard)
              .type(StampTransactionType.EARN)
              .stamps(i + 1)
              .stampsAfter(i + 1)
              .rewardsAfter(i >= 5 ? 1 : 0)
              .description("Tích tem từ sử dụng box")
              .build());
    }

    // Service stamp card with redemption
    if (accounts.size() > 3) {
      User user = accounts.get(3).getUser();
      StampCard svcCard =
          stampCardRepository.save(
              StampCard.builder()
                  .user(user)
                  .stampType(StampType.SERVICE)
                  .service(allServices.get(3))
                  .stampsRequired(6)
                  .currentStamps(0)
                  .freeRewardsAvailable(0)
                  .totalStampsEarned(6)
                  .totalRewardsRedeemed(1)
                  .build());
      count++;

      stampTransactionRepository.save(
          StampTransaction.builder()
              .user(user)
              .stampCard(svcCard)
              .type(StampTransactionType.REDEEM)
              .stamps(-6)
              .stampsAfter(0)
              .rewardsAfter(0)
              .discountApplied(new BigDecimal("15000"))
              .description("Đổi 1 lần giặt miễn phí")
              .build());
    }

    return count;
  }

  // ==================== NOTIFICATIONS ====================
  private int createNotifications() {
    int count = 0;

    // Order notifications
    for (int i = 0; i < Math.min(10, allOrders.size()); i++) {
      Order order = allOrders.get(i);
      String title =
          switch (order.getStatus()) {
            case COMPLETED -> "Đơn hàng hoàn thành";
            case WAITING -> "Đơn hàng đang chờ xử lý";
            case PROCESSING -> "Đơn hàng đang được giặt";
            case RETURNED -> "Đồ đã sẵn sàng lấy";
            case CANCELED -> "Đơn hàng đã hủy";
            default -> "Cập nhật đơn hàng";
          };

      notificationRepository.save(
          Notification.builder()
              .user(order.getSender())
              .type(NotificationType.ORDER_STATUS)
              .title(title)
              .message("Đơn hàng #" + order.getId() + " - " + order.getDescription())
              .referenceId(order.getId())
              .referenceType("ORDER")
              .status(i < 5 ? NotificationStatus.READ : NotificationStatus.UNREAD)
              .createdAt(order.getCreatedAt())
              .readAt(i < 5 ? LocalDateTime.now().minusHours(i) : null)
              .build());
      count++;
    }

    // Payment notifications
    List<Payment> payments = paymentRepository.findAll();
    for (Payment payment : payments.subList(0, Math.min(3, payments.size()))) {
      notificationRepository.save(
          Notification.builder()
              .user(payment.getCustomer())
              .type(NotificationType.PAYMENT)
              .title(
                  payment.getStatus() == PaymentStatus.COMPLETED
                      ? "Thanh toán thành công"
                      : "Thanh toán thất bại")
              .message(
                  "Đơn hàng #" + payment.getOrder().getId() + " - " + payment.getAmount() + " VND")
              .referenceId(payment.getId())
              .referenceType("PAYMENT")
              .status(NotificationStatus.UNREAD)
              .build());
      count++;
    }

    // System notifications
    for (int i = 4; i < Math.min(8, allUsers.size()); i++) {
      notificationRepository.save(
          Notification.builder()
              .user(allUsers.get(i))
              .type(NotificationType.SYSTEM)
              .title("Khuyến mãi tháng 1")
              .message("Giảm 20% cho đơn hàng đầu tiên! Áp dụng đến 31/01/2026")
              .status(NotificationStatus.UNREAD)
              .build());
      count++;
    }

    return count;
  }

  // ==================== HELPERS ====================
  private Role findRole(List<Role> roles, RoleName name) {
    return roles.stream().filter(r -> r.getName() == name).findFirst().orElseThrow();
  }

  private User findUserByEmail(String email) {
    return allUsers.stream().filter(u -> email.equals(u.getEmail())).findFirst().orElseThrow();
  }

  private void printSummary() {
    log.info("");
    log.info("📊 DATA SUMMARY:");
    log.info("┌──────────────────────────────────────────────┐");
    log.info("│ Entity              │ Count                  │");
    log.info("├──────────────────────────────────────────────┤");
    log.info("│ Users               │ {}                     │", userRepository.count());
    log.info("│ Partners            │ {} (2 approved, 1 pending) │", partnerRepository.count());
    log.info("│ Stores              │ {} (4 active, 1 inactive)  │", storeRepository.count());
    log.info("│ Lockers             │ {} (various statuses)      │", lockerRepository.count());
    log.info("│ Boxes               │ {}                     │", boxRepository.count());
    log.info("│ Services            │ {}                     │", serviceRepository.count());
    log.info("│ Orders              │ {} (all statuses)      │", orderRepository.count());
    log.info("│ Payments            │ {}                     │", paymentRepository.count());
    log.info("│ Access Codes        │ {}                     │", accessCodeRepository.count());
    log.info("│ Loyalty Accounts    │ {}                     │", loyaltyAccountRepository.count());
    log.info(
        "│ Point Transactions  │ {}                     │", pointTransactionRepository.count());
    log.info("│ Stamp Cards         │ {}                     │", stampCardRepository.count());
    log.info("│ Notifications       │ {}                     │", notificationRepository.count());
    log.info("└──────────────────────────────────────────────┘");
    log.info("");
    log.info("🔐 TEST ACCOUNTS (password: {}):", DEFAULT_PASSWORD);
    log.info("   Admin:    admin@laundrylocker.com");
    log.info("   Partners: partner.minh@gmail.com, partner.huong@gmail.com");
    log.info("   Customers: customer.huy@gmail.com, customer.lan@gmail.com, ...");
    log.info("");
    log.info("📝 SAMPLE DATA:");
    log.info("   • WAITING orders: PIN 123456, 234567, 345678");
    log.info("   • RETURNED orders: PIN 654321, 765432");
    log.info("   • Active access codes: ABC12XYZ, DEF34UVW, GHI56RST");
    log.info("   • Return code: RET12345");
  }
}
