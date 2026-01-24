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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/** Sample data loader for dev and docker profiles. */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SampleDataLoader {

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
  private final PasswordEncoder passwordEncoder;

  @Value("${app.data.load-sample-data:false}")
  private boolean loadSampleData;

  @Value("${app.data.force-reload:false}")
  private boolean forceReload;

  private static final String DEFAULT_PASSWORD = "password123";

  @Bean
  @Profile({"dev", "docker"})
  public CommandLineRunner loadSampleData() {
    return args -> {
      log.info("========================================");
      log.info("📦 Sample Data Loader");
      log.info("========================================");
      log.info("   load-sample-data: {}", loadSampleData);
      log.info("   force-reload: {}", forceReload);

      if (!loadSampleData) {
        log.info("   Status: DISABLED");
        log.info("   Set app.data.load-sample-data=true to enable.");
        log.info("========================================");
        return;
      }

      long userCount = userRepository.count();
      log.info("   Existing users: {}", userCount);

      if (userCount > 0 && !forceReload) {
        log.info("   Status: SKIPPED (data already exists)");
        log.info("   Set app.data.force-reload=true to reload.");
        log.info("========================================");
        return;
      }

      if (forceReload && userCount > 0) {
        log.info("   Force reload enabled, clearing existing data...");
        clearExistingData();
      }

      log.info("🚀 Loading sample data...");
      loadAllData();
      log.info("✅ Sample data loaded successfully!");
      printTestAccounts();
      log.info("========================================");
    };
  }

  @Transactional
  public void clearExistingData() {
    accessCodeRepository.deleteAll();
    orderDetailRepository.deleteAll();
    orderRepository.deleteAll();
    boxRepository.deleteAll();
    lockerRepository.deleteAll();
    serviceRepository.deleteAll();
    storeRepository.deleteAll();
    partnerRepository.deleteAll();
    userRepository.deleteAll();
    log.info("   Existing data cleared.");
  }

  @Transactional
  public void loadAllData() {
    // 1. Roles
    List<Role> roles = createRoles();
    log.info("   ✓ Created {} roles", roles.size());

    // 2. Users
    List<User> users = createUsers(roles);
    log.info("   ✓ Created {} users", users.size());

    // 3. Partners
    List<Partner> partners = createPartners(users);
    log.info("   ✓ Created {} partners", partners.size());

    // 4. Stores
    List<Store> stores = createStores(partners);
    log.info("   ✓ Created {} stores", stores.size());

    // 5. Services (need store)
    List<LaundryService> services = createServices(stores.get(0));
    log.info("   ✓ Created {} services", services.size());

    // 6. Lockers & Boxes
    List<Locker> lockers = createLockers(stores);
    log.info("   ✓ Created {} lockers", lockers.size());

    // 7. Orders
    List<Order> orders = createOrders(users, lockers, services);
    log.info("   ✓ Created {} orders", orders.size());

    // 8. Access Codes
    createAccessCodes(orders, partners);
    log.info("   ✓ Created sample access codes");
  }

  private List<Role> createRoles() {
    return List.of(
        saveRole(RoleName.ADMIN),
        saveRole(RoleName.USER),
        saveRole(RoleName.PARTNER),
        saveRole(RoleName.STAFF));
  }

  private Role saveRole(RoleName name) {
    return roleRepository
        .findByName(name)
        .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
  }

  private List<User> createUsers(List<Role> roles) {
    Role adminRole = findRole(roles, RoleName.ADMIN);
    Role userRole = findRole(roles, RoleName.USER);
    Role partnerRole = findRole(roles, RoleName.PARTNER);

    String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

    return List.of(
        createUser(
            "admin@laundrylocker.com",
            "System",
            "Admin",
            "0900000001",
            encodedPassword,
            Set.of(adminRole)),
        createUser(
            "partner.minh@gmail.com",
            "Minh",
            "Nguyễn Văn",
            "0900000002",
            encodedPassword,
            Set.of(userRole, partnerRole)),
        createUser(
            "partner.huong@gmail.com",
            "Hương",
            "Trần Thị",
            "0900000003",
            encodedPassword,
            Set.of(userRole, partnerRole)),
        createUser(
            "customer.huy@gmail.com",
            "Huy",
            "Lê Văn",
            "0900000004",
            encodedPassword,
            Set.of(userRole)),
        createUser(
            "customer.lan@gmail.com",
            "Lan",
            "Phạm Thị",
            "0900000005",
            encodedPassword,
            Set.of(userRole)),
        createUser(
            "customer.tuan@gmail.com",
            "Tuấn",
            "Võ Minh",
            "0900000006",
            encodedPassword,
            Set.of(userRole)),
        createUser(
            "customer.mai@gmail.com",
            "Mai",
            "Nguyễn Thị",
            "0900000007",
            encodedPassword,
            Set.of(userRole)),
        createUser(
            "customer.duc@gmail.com",
            "Đức",
            "Trần Văn",
            "0900000008",
            encodedPassword,
            Set.of(userRole)));
  }

  private User createUser(
      String email,
      String firstName,
      String lastName,
      String phone,
      String password,
      Set<Role> roles) {
    return userRepository.save(
        User.builder()
            .email(email)
            .name(firstName + " " + lastName)
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(phone)
            .password(password)
            .provider(AuthProvider.LOCAL)
            .emailVerified(true)
            .roles(new HashSet<>(roles))
            .build());
  }

  private List<Partner> createPartners(List<User> users) {
    User minh = findUserByEmail(users, "partner.minh@gmail.com");
    User huong = findUserByEmail(users, "partner.huong@gmail.com");
    User admin = findUserByEmail(users, "admin@laundrylocker.com");

    return List.of(
        partnerRepository.save(
            Partner.builder()
                .user(minh)
                .businessName("Giặt Ủi Sạch Sẽ Q1")
                .businessRegistrationNumber("BRN-001-2024")
                .taxId("TAX-001-2024")
                .businessAddress("123 Nguyễn Huệ, Quận 1, TP.HCM")
                .contactPhone("0900000002")
                .contactEmail("partner.minh@gmail.com")
                .status(PartnerStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .approvedBy(admin.getId())
                .revenueSharePercent(new BigDecimal("70.00"))
                .build()),
        partnerRepository.save(
            Partner.builder()
                .user(huong)
                .businessName("Tiệm Giặt Hương Sắc Q3")
                .businessRegistrationNumber("BRN-002-2024")
                .taxId("TAX-002-2024")
                .businessAddress("456 Võ Văn Tần, Quận 3, TP.HCM")
                .contactPhone("0900000003")
                .contactEmail("partner.huong@gmail.com")
                .status(PartnerStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .approvedBy(admin.getId())
                .revenueSharePercent(new BigDecimal("70.00"))
                .build()));
  }

  private List<Store> createStores(List<Partner> partners) {
    Partner p1 = partners.get(0);
    Partner p2 = partners.get(1);

    return List.of(
        storeRepository.save(
            Store.builder()
                .name("Cửa hàng Quận 1 - Nguyễn Huệ")
                .address("123 Nguyễn Huệ, Quận 1, TP.HCM")
                .contactPhone("0281234567")
                .latitude(10.7769)
                .longitude(106.7009)
                .status(StoreStatus.ACTIVE)
                .partner(p1)
                .build()),
        storeRepository.save(
            Store.builder()
                .name("Cửa hàng Quận 1 - Lê Lợi")
                .address("789 Lê Lợi, Quận 1, TP.HCM")
                .contactPhone("0281234568")
                .latitude(10.7731)
                .longitude(106.7012)
                .status(StoreStatus.ACTIVE)
                .partner(p1)
                .build()),
        storeRepository.save(
            Store.builder()
                .name("Cửa hàng Quận 3 - Võ Văn Tần")
                .address("456 Võ Văn Tần, Quận 3, TP.HCM")
                .contactPhone("0281234569")
                .latitude(10.7756)
                .longitude(106.6863)
                .status(StoreStatus.ACTIVE)
                .partner(p2)
                .build()));
  }

  private List<Locker> createLockers(List<Store> stores) {
    Store s1 = stores.get(0);
    Store s2 = stores.get(1);
    Store s3 = stores.get(2);

    Locker l1 =
        createLockerWithBoxes(
            "LOC-Q1-NH-01",
            "Tủ A - Nguyễn Huệ",
            s1,
            new BoxSize[] {
              BoxSize.SMALL,
              BoxSize.SMALL,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.LARGE,
              BoxSize.LARGE,
              BoxSize.EXTRA_LARGE
            });

    Locker l2 =
        createLockerWithBoxes(
            "LOC-Q1-NH-02",
            "Tủ B - Nguyễn Huệ",
            s1,
            new BoxSize[] {
              BoxSize.SMALL,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.LARGE,
              BoxSize.LARGE,
              BoxSize.EXTRA_LARGE
            });

    Locker l3 =
        createLockerWithBoxes(
            "LOC-Q1-LL-01",
            "Tủ A - Lê Lợi",
            s2,
            new BoxSize[] {
              BoxSize.SMALL,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.LARGE,
              BoxSize.LARGE,
              BoxSize.EXTRA_LARGE
            });

    Locker l4 =
        createLockerWithBoxes(
            "LOC-Q3-VVT-01",
            "Tủ A - Võ Văn Tần",
            s3,
            new BoxSize[] {
              BoxSize.SMALL,
              BoxSize.MEDIUM,
              BoxSize.MEDIUM,
              BoxSize.LARGE,
              BoxSize.LARGE,
              BoxSize.EXTRA_LARGE
            });

    return List.of(l1, l2, l3, l4);
  }

  private Locker createLockerWithBoxes(String code, String name, Store store, BoxSize[] sizes) {
    Locker locker =
        lockerRepository.save(
            Locker.builder()
                .code(code)
                .name(name)
                .store(store)
                .status(LockerStatus.ACTIVE)
                .build());

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

  private List<LaundryService> createServices(Store store) {
    return List.of(
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
            store),
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
            store),
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
            store),
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
            store),
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
            store),
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
            store),
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
            store),
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
            store));
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

  private List<Order> createOrders(
      List<User> users, List<Locker> lockers, List<LaundryService> services) {
    User huy = findUserByEmail(users, "customer.huy@gmail.com");
    User lan = findUserByEmail(users, "customer.lan@gmail.com");
    User tuan = findUserByEmail(users, "customer.tuan@gmail.com");
    User mai = findUserByEmail(users, "customer.mai@gmail.com");
    User duc = findUserByEmail(users, "customer.duc@gmail.com");

    Locker locker1 = lockers.get(0);
    Locker locker2 = lockers.get(1);

    List<Box> boxes1 = boxRepository.findByLockerId(locker1.getId());
    List<Box> boxes2 = boxRepository.findByLockerId(locker2.getId());

    LaundryService storageService = services.get(0);
    LaundryService laundryService = services.get(3);
    LaundryService premiumService = services.get(4);
    LaundryService largeService = services.get(5);

    // Order 1: COMPLETED
    Order order1 =
        createOrder(
            huy,
            locker1,
            null,
            null,
            OrderStatus.COMPLETED,
            null,
            2.50,
            new BigDecimal("42500"),
            "Giặt quần áo hàng ngày",
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now().minusDays(5));
    createOrderDetail(order1, storageService, 1.0, new BigDecimal("5000"));
    createOrderDetail(order1, laundryService, 2.5, new BigDecimal("15000"));

    // Order 2: WAITING
    Box box3 = boxes1.get(2);
    box3.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(box3);

    Order order2 =
        createOrder(
            lan,
            locker1,
            box3,
            null,
            OrderStatus.WAITING,
            "123456",
            null,
            new BigDecimal("65000"),
            "Giặt áo vest và quần tây",
            LocalDateTime.now().minusHours(1),
            null);
    createOrderDetail(order2, storageService, 1.0, new BigDecimal("5000"));
    createOrderDetail(order2, premiumService, 2.0, new BigDecimal("30000"));

    // Order 3: PROCESSING
    Order order3 =
        createOrder(
            tuan,
            locker1,
            null,
            null,
            OrderStatus.PROCESSING,
            null,
            3.00,
            new BigDecimal("55000"),
            "Giặt chăn mền",
            LocalDateTime.now().minusDays(2),
            null);
    createOrderDetail(order3, storageService, 1.0, new BigDecimal("5000"));
    createOrderDetail(order3, largeService, 1.0, new BigDecimal("50000"));

    // Order 4: RETURNED
    Box box6 = boxes1.get(5);
    box6.setStatus(BoxStatus.OCCUPIED);
    boxRepository.save(box6);

    Order order4 =
        createOrder(
            mai,
            locker1,
            null,
            box6,
            OrderStatus.RETURNED,
            "654321",
            1.50,
            new BigDecimal("32500"),
            "Giặt đồ công sở",
            LocalDateTime.now().minusDays(3),
            null);
    createOrderDetail(order4, storageService, 1.0, new BigDecimal("5000"));
    createOrderDetail(order4, laundryService, 1.5, new BigDecimal("15000"));

    // Order 5: INITIALIZED
    Box box10 = boxes2.get(1);
    box10.setStatus(BoxStatus.RESERVED);
    boxRepository.save(box10);

    Order order5 =
        createOrder(
            duc,
            locker2,
            box10,
            null,
            OrderStatus.INITIALIZED,
            "111222",
            null,
            new BigDecimal("17000"),
            "Giặt quần áo thể thao",
            LocalDateTime.now().minusMinutes(10),
            null);
    createOrderDetail(order5, storageService, 1.0, new BigDecimal("5000"));
    createOrderDetail(order5, laundryService, 1.0, new BigDecimal("12000"));

    return List.of(order1, order2, order3, order4, order5);
  }

  private Order createOrder(
      User sender,
      Locker locker,
      Box sendBox,
      Box receiveBox,
      OrderStatus status,
      String pinCode,
      Double weight,
      BigDecimal totalPrice,
      String desc,
      LocalDateTime createdAt,
      LocalDateTime completedAt) {
    Order order =
        Order.builder()
            .type(OrderType.LAUNDRY)
            .sender(sender)
            .locker(locker)
            .sendBox(sendBox)
            .receiveBox(receiveBox)
            .status(status)
            .pinCode(pinCode)
            .pinCodeIssuedAt(pinCode != null ? LocalDateTime.now() : null)
            .actualWeight(weight != null ? new BigDecimal(weight) : null)
            .weightUnit(weight != null ? "kg" : null)
            .reservationFee(new BigDecimal("5000"))
            .storagePrice(new BigDecimal("5000"))
            .totalPrice(totalPrice)
            .description(desc)
            .completedAt(completedAt)
            .build();
    order.setCreatedAt(createdAt);
    return orderRepository.save(order);
  }

  private void createOrderDetail(
      Order order, LaundryService service, Double qty, BigDecimal price) {
    orderDetailRepository.save(
        OrderDetail.builder().order(order).service(service).quantity(qty).price(price).build());
  }

  private void createAccessCodes(List<Order> orders, List<Partner> partners) {
    Order order2 = orders.get(1);
    Partner partner1 = partners.get(0);

    accessCodeRepository.save(
        StaffAccessCode.builder()
            .code("ABC12XYZ")
            .order(order2)
            .partner(partner1)
            .action(AccessCodeAction.COLLECT)
            .status(AccessCodeStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .build());
  }

  private Role findRole(List<Role> roles, RoleName name) {
    return roles.stream().filter(r -> r.getName() == name).findFirst().orElseThrow();
  }

  private User findUserByEmail(List<User> users, String email) {
    return users.stream().filter(u -> email.equals(u.getEmail())).findFirst().orElseThrow();
  }

  private void printTestAccounts() {
    log.info("📊 Sample data summary:");
    log.info("   • 8 users, 2 partners, 3 stores, 4 lockers, 26 boxes");
    log.info("   • 8 services, 5 orders (various statuses)");
    log.info("");
    log.info("🔐 Test accounts (password: {}):", DEFAULT_PASSWORD);
    log.info("   • Admin:    admin@laundrylocker.com");
    log.info("   • Partner:  partner.minh@gmail.com");
    log.info("   • Partner:  partner.huong@gmail.com");
    log.info("   • Customer: customer.huy@gmail.com");
    log.info("   • Customer: customer.lan@gmail.com (has WAITING order)");
    log.info("   • Customer: customer.mai@gmail.com (has RETURNED order, PIN: 654321)");
    log.info("");
    log.info("🎫 Sample access code: ABC12XYZ (for order #2)");
  }
}
