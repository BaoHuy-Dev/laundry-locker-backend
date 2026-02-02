package com.huynqb.laundrylockerbackend.core.config;

import com.huynqb.laundrylockerbackend.module.admin.entity.AuditLog;
import com.huynqb.laundrylockerbackend.module.admin.entity.Promotion;
import com.huynqb.laundrylockerbackend.module.admin.entity.PromotionUsage;
import com.huynqb.laundrylockerbackend.module.admin.repository.AuditLogRepository;
import com.huynqb.laundrylockerbackend.module.admin.repository.PromotionRepository;
import com.huynqb.laundrylockerbackend.module.admin.repository.PromotionUsageRepository;
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
import com.huynqb.laundrylockerbackend.module.order.entity.OrderRating;
import com.huynqb.laundrylockerbackend.module.order.entity.OrderStatusHistory;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderType;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.model.OrderDetail;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderDetailRepository;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRatingRepository;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderStatusHistoryRepository;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeStatus;
import com.huynqb.laundrylockerbackend.module.partner.enums.PartnerStatus;
import com.huynqb.laundrylockerbackend.module.partner.model.Partner;
import com.huynqb.laundrylockerbackend.module.partner.model.StaffAccessCode;
import com.huynqb.laundrylockerbackend.module.partner.repository.PartnerRepository;
import com.huynqb.laundrylockerbackend.module.partner.repository.StaffAccessCodeRepository;
import com.huynqb.laundrylockerbackend.module.payment.entity.Refund;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentMethod;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentStatus;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import com.huynqb.laundrylockerbackend.module.payment.repository.PaymentRepository;
import com.huynqb.laundrylockerbackend.module.payment.repository.RefundRepository;
import com.huynqb.laundrylockerbackend.module.store.enums.StoreStatus;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.model.Permission;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.PermissionRepository;
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
  private final PermissionRepository permissionRepository;
  private final UserRepository userRepository;
  private final PartnerRepository partnerRepository;
  private final StoreRepository storeRepository;
  private final LockerRepository lockerRepository;
  private final BoxRepository boxRepository;
  private final LaundryServiceRepository serviceRepository;
  private final OrderRepository orderRepository;
  private final OrderDetailRepository orderDetailRepository;
  private final OrderRatingRepository orderRatingRepository;
  private final OrderStatusHistoryRepository orderStatusHistoryRepository;
  private final StaffAccessCodeRepository accessCodeRepository;
  private final PaymentRepository paymentRepository;
  private final RefundRepository refundRepository;
  private final NotificationRepository notificationRepository;
  private final LoyaltyAccountRepository loyaltyAccountRepository;
  private final PointTransactionRepository pointTransactionRepository;
  private final StampCardRepository stampCardRepository;
  private final StampTransactionRepository stampTransactionRepository;
  private final PromotionRepository promotionRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final AuditLogRepository auditLogRepository;
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
  private List<Payment> allPayments;
  private List<Promotion> allPromotions;

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

      // Check if sample data already loaded (more than just admin user)
      boolean sampleDataExists = userCount > 5;

      if (sampleDataExists && !forceReload) {
        log.info("   Status: SKIPPED (sample data exists)");
        log.info("   Set app.data.force-reload=true to reload.");
        log.info("========================================");
        return;
      }

      if (forceReload && sampleDataExists) {
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
    // Clear in order of dependencies
    auditLogRepository.deleteAll();
    promotionUsageRepository.deleteAll();
    promotionRepository.deleteAll();
    refundRepository.deleteAll();
    stampTransactionRepository.deleteAll();
    stampCardRepository.deleteAll();
    pointTransactionRepository.deleteAll();
    loyaltyAccountRepository.deleteAll();
    notificationRepository.deleteAll();
    orderRatingRepository.deleteAll();
    orderStatusHistoryRepository.deleteAll();
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
    roleRepository.deleteAll();
    permissionRepository.deleteAll();
    log.info("   ✓ All data cleared");
  }

  @Transactional
  public void loadAllData() {
    // 1. Permissions (20)
    int permissions = createPermissions();
    log.info("   ✓ {} permissions", permissions);

    // 2. Roles (5)
    List<Role> roles = createRoles();
    log.info("   ✓ {} roles", roles.size());

    // 3. Users (25 users)
    allUsers = createUsers(roles);
    log.info("   ✓ {} users", allUsers.size());

    // 4. Partners (20 partners)
    allPartners = createPartners();
    log.info("   ✓ {} partners", allPartners.size());

    // 5. Stores (20 stores)
    allStores = createStores();
    log.info("   ✓ {} stores", allStores.size());

    // 6. Lockers & Boxes (20 lockers, ~100 boxes)
    allLockers = createLockers();
    log.info("   ✓ {} lockers with boxes", allLockers.size());

    // 7. Services (20+ services)
    allServices = createServices();
    log.info("   ✓ {} services", allServices.size());

    // 8. Orders (25 orders with various statuses)
    allOrders = createOrders();
    log.info("   ✓ {} orders", allOrders.size());

    // 9. Order Status History (20+)
    int statusHistory = createOrderStatusHistory();
    log.info("   ✓ {} order status history", statusHistory);

    // 10. Order Ratings (20)
    int ratings = createOrderRatings();
    log.info("   ✓ {} order ratings", ratings);

    // 11. Payments (20+)
    allPayments = createPayments();
    log.info("   ✓ {} payments", allPayments.size());

    // 12. Refunds (20)
    int refunds = createRefunds();
    log.info("   ✓ {} refunds", refunds);

    // 13. Staff Access Codes (20+)
    int codes = createAccessCodes();
    log.info("   ✓ {} access codes", codes);

    // 14. Promotions (20)
    allPromotions = createPromotions();
    log.info("   ✓ {} promotions", allPromotions.size());

    // 15. Promotion Usage (20)
    int promoUsage = createPromotionUsage();
    log.info("   ✓ {} promotion usage", promoUsage);

    // 16. Loyalty Accounts (20)
    int loyaltyAccounts = createLoyaltyAccounts();
    log.info("   ✓ {} loyalty accounts", loyaltyAccounts);

    // 17. Point Transactions (20+)
    int pointTxns = createPointTransactions();
    log.info("   ✓ {} point transactions", pointTxns);

    // 18. Stamp Cards (20+)
    int stampCards = createStampCards();
    log.info("   ✓ {} stamp cards", stampCards);

    // 19. Notifications (20+)
    int notifications = createNotifications();
    log.info("   ✓ {} notifications", notifications);

    // 20. Audit Logs (20+)
    int auditLogs = createAuditLogs();
    log.info("   ✓ {} audit logs", auditLogs);
  }

  // ==================== PERMISSIONS ====================
  private int createPermissions() {
    if (permissionRepository.count() > 0) {
      return (int) permissionRepository.count();
    }
    String[][] perms = {
      {"READ_USERS", "Xem danh sách người dùng"},
      {"WRITE_USERS", "Tạo/sửa người dùng"},
      {"DELETE_USERS", "Xóa người dùng"},
      {"READ_ORDERS", "Xem đơn hàng"},
      {"WRITE_ORDERS", "Tạo/sửa đơn hàng"},
      {"DELETE_ORDERS", "Xóa đơn hàng"},
      {"READ_STORES", "Xem cửa hàng"},
      {"WRITE_STORES", "Tạo/sửa cửa hàng"},
      {"DELETE_STORES", "Xóa cửa hàng"},
      {"READ_LOCKERS", "Xem tủ locker"},
      {"WRITE_LOCKERS", "Tạo/sửa tủ locker"},
      {"DELETE_LOCKERS", "Xóa tủ locker"},
      {"READ_PAYMENTS", "Xem thanh toán"},
      {"PROCESS_REFUNDS", "Xử lý hoàn tiền"},
      {"READ_REPORTS", "Xem báo cáo"},
      {"MANAGE_PARTNERS", "Quản lý đối tác"},
      {"MANAGE_STAFF", "Quản lý nhân viên"},
      {"MANAGE_PROMOTIONS", "Quản lý khuyến mãi"},
      {"VIEW_AUDIT_LOGS", "Xem audit logs"},
      {"SYSTEM_ADMIN", "Quyền quản trị hệ thống"}
    };
    int count = 0;
    for (String[] p : perms) {
      permissionRepository.save(Permission.builder().name(p[0]).description(p[1]).build());
      count++;
    }
    return count;
  }

  // ==================== ROLES ====================
  private List<Role> createRoles() {
    List<Permission> allPermissions = permissionRepository.findAll();

    // Define permissions per role
    Set<String> adminPerms =
        Set.of(
            "READ_USERS",
            "WRITE_USERS",
            "DELETE_USERS",
            "READ_ORDERS",
            "WRITE_ORDERS",
            "DELETE_ORDERS",
            "READ_STORES",
            "WRITE_STORES",
            "DELETE_STORES",
            "READ_LOCKERS",
            "WRITE_LOCKERS",
            "DELETE_LOCKERS",
            "READ_PAYMENTS",
            "PROCESS_REFUNDS",
            "READ_REPORTS",
            "MANAGE_PARTNERS",
            "MANAGE_STAFF",
            "MANAGE_PROMOTIONS",
            "VIEW_AUDIT_LOGS",
            "SYSTEM_ADMIN");

    Set<String> partnerPerms =
        Set.of(
            "READ_ORDERS",
            "WRITE_ORDERS",
            "READ_STORES",
            "WRITE_STORES",
            "READ_LOCKERS",
            "WRITE_LOCKERS",
            "READ_PAYMENTS",
            "MANAGE_STAFF",
            "MANAGE_PROMOTIONS",
            "READ_REPORTS");

    Set<String> staffPerms =
        Set.of("READ_ORDERS", "WRITE_ORDERS", "READ_STORES", "READ_LOCKERS", "READ_PAYMENTS");

    Set<String> userPerms = Set.of("READ_ORDERS", "READ_STORES", "READ_LOCKERS", "READ_PAYMENTS");

    Set<String> moderatorPerms =
        Set.of(
            "READ_USERS",
            "READ_ORDERS",
            "WRITE_ORDERS",
            "READ_STORES",
            "READ_LOCKERS",
            "READ_PAYMENTS",
            "READ_REPORTS",
            "VIEW_AUDIT_LOGS");

    return List.of(
        saveRoleWithPermissions(RoleName.ADMIN, allPermissions, adminPerms),
        saveRoleWithPermissions(RoleName.USER, allPermissions, userPerms),
        saveRoleWithPermissions(RoleName.PARTNER, allPermissions, partnerPerms));
  }

  private Role saveRoleWithPermissions(
      RoleName name, List<Permission> allPermissions, Set<String> permNames) {
    Role role =
        roleRepository
            .findByName(name)
            .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));

    // Add permissions if not already set
    if (role.getPermissions().isEmpty()) {
      Set<Permission> perms =
          allPermissions.stream()
              .filter(p -> permNames.contains(p.getName()))
              .collect(java.util.stream.Collectors.toSet());
      role.setPermissions(perms);
      role = roleRepository.save(role);
    }
    return role;
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

    String pwd = passwordEncoder.encode(DEFAULT_PASSWORD);

    List<User> users = new ArrayList<>();

    // Admin (1)
    users.add(
        createUser(
            "baohuy2k12k4@gmail.com", "System", "Admin", "0900000001", pwd, Set.of(adminRole)));

    // Partners (5)
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
    users.add(
        createUser(
            "partner.thao@gmail.com",
            "Thảo",
            "Nguyễn Thị",
            "0900000005",
            pwd,
            Set.of(userRole, partnerRole)));
    users.add(
        createUser(
            "partner.binh@gmail.com",
            "Bình",
            "Phạm Văn",
            "0900000006",
            pwd,
            Set.of(userRole, partnerRole)));

    // Customers (15)
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
    users.add(
        createUser(
            "customer.hung@gmail.com", "Hùng", "Trần Văn", "0901000011", pwd, Set.of(userRole)));
    users.add(
        createUser("customer.nga@gmail.com", "Nga", "Lê Thị", "0901000012", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.phong@gmail.com",
            "Phong",
            "Nguyễn Văn",
            "0901000013",
            pwd,
            Set.of(userRole)));
    users.add(
        createUser(
            "customer.vy@gmail.com", "Vy", "Hoàng Thị", "0901000014", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "customer.cuong@gmail.com", "Cường", "Đỗ Văn", "0901000015", pwd, Set.of(userRole)));

    // Additional regular users (can be assigned as internal staff by Partner)
    users.add(
        createUser(
            "staff.tung@gmail.com", "Tùng", "Nguyễn Văn", "0902000001", pwd, Set.of(userRole)));
    users.add(
        createUser(
            "staff.hien@gmail.com", "Hiền", "Trần Thị", "0902000002", pwd, Set.of(userRole)));
    users.add(
        createUser("staff.tai@gmail.com", "Tài", "Lê Văn", "0902000003", pwd, Set.of(userRole)));

    return users;
  }

  private User createUser(
      String email, String firstName, String lastName, String phone, String pwd, Set<Role> roles) {
    // Check if user already exists (e.g., admin created by AdminBootstrapConfig)
    return userRepository
        .findByEmail(email)
        .orElseGet(
            () ->
                userRepository.save(
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
                        .build()));
  }

  // ==================== PARTNERS ====================
  private List<Partner> createPartners() {
    User admin = findUserByEmail("baohuy2k12k4@gmail.com");
    List<Partner> partners = new ArrayList<>();

    // Get partner users
    String[] partnerEmails = {
      "partner.minh@gmail.com",
      "partner.huong@gmail.com",
      "partner.nam@gmail.com",
      "partner.thao@gmail.com",
      "partner.binh@gmail.com"
    };
    String[][] businessInfo = {
      {
        "Giặt Ủi Sạch Sẽ - Quận 1",
        "BRN-Q1-001",
        "TAX-Q1-001",
        "123 Nguyễn Huệ, Quận 1",
        "APPROVED",
        "70.00"
      },
      {
        "Tiệm Giặt Hương Sắc - Quận 3",
        "BRN-Q3-001",
        "TAX-Q3-001",
        "456 Võ Văn Tần, Quận 3",
        "APPROVED",
        "65.00"
      },
      {
        "Clean & Fresh Laundry - Quận 7",
        "BRN-Q7-001",
        "TAX-Q7-001",
        "789 Nguyễn Văn Linh, Quận 7",
        "PENDING",
        "70.00"
      },
      {
        "Giặt Ủi Thảo Nguyên - Quận 2",
        "BRN-Q2-001",
        "TAX-Q2-001",
        "100 Thảo Điền, Quận 2",
        "APPROVED",
        "68.00"
      },
      {
        "Bình Minh Laundry - Quận 9",
        "BRN-Q9-001",
        "TAX-Q9-001",
        "200 Lê Văn Việt, Quận 9",
        "APPROVED",
        "65.00"
      }
    };

    for (int i = 0; i < partnerEmails.length; i++) {
      User user = findUserByEmail(partnerEmails[i]);
      String[] info = businessInfo[i];
      PartnerStatus status =
          "APPROVED".equals(info[4]) ? PartnerStatus.APPROVED : PartnerStatus.PENDING;
      Partner partner =
          partnerRepository.save(
              Partner.builder()
                  .user(user)
                  .businessName(info[0])
                  .businessRegistrationNumber(info[1])
                  .taxId(info[2])
                  .businessAddress(info[3] + ", TP.HCM")
                  .contactPhone(user.getPhoneNumber())
                  .contactEmail(user.getEmail())
                  .status(status)
                  .approvedAt(
                      status == PartnerStatus.APPROVED
                          ? LocalDateTime.now().minusMonths(i + 1)
                          : null)
                  .approvedBy(status == PartnerStatus.APPROVED ? admin.getId() : null)
                  .revenueSharePercent(new BigDecimal(info[5]))
                  .build());
      partners.add(partner);
    }

    // Add 15 more partners from customers (promoted)
    for (int i = 0; i < 15; i++) {
      int idx = 6 + i; // Start from customer.huy
      if (idx >= allUsers.size()) break;
      User customer = allUsers.get(idx);
      Partner partner =
          partnerRepository.save(
              Partner.builder()
                  .user(customer)
                  .businessName("Tiệm Giặt " + customer.getFirstName() + " - Q" + ((i % 12) + 1))
                  .businessRegistrationNumber("BRN-AUTO-" + String.format("%03d", i + 6))
                  .taxId("TAX-AUTO-" + String.format("%03d", i + 6))
                  .businessAddress(
                      (100 + i * 10)
                          + " Đường "
                          + (i + 1)
                          + ", Quận "
                          + ((i % 12) + 1)
                          + ", TP.HCM")
                  .contactPhone(customer.getPhoneNumber())
                  .contactEmail(customer.getEmail())
                  .status(i % 3 == 0 ? PartnerStatus.PENDING : PartnerStatus.APPROVED)
                  .approvedAt(i % 3 != 0 ? LocalDateTime.now().minusDays(i * 7) : null)
                  .approvedBy(i % 3 != 0 ? admin.getId() : null)
                  .revenueSharePercent(new BigDecimal(60 + (i % 15)))
                  .build());
      partners.add(partner);
    }

    // Assign staff to partners (partner_staff junction table)
    assignStaffToPartners(partners);

    return partners;
  }

  private void assignStaffToPartners(List<Partner> partners) {
    // Note: Staff is now external actor managed via Access Codes
    // Partner can add any USER to their internal staff list
    // Get some regular users to assign as internal staff
    List<User> regularUsers =
        allUsers.stream()
            .filter(
                u ->
                    u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.USER)
                        && u.getRoles().stream().noneMatch(r -> r.getName() == RoleName.PARTNER)
                        && u.getRoles().stream().noneMatch(r -> r.getName() == RoleName.ADMIN))
            .limit(10)
            .toList();

    if (regularUsers.isEmpty()) return;

    // Distribute users as internal staff among approved partners
    List<Partner> approvedPartners =
        partners.stream().filter(p -> p.getStatus() == PartnerStatus.APPROVED).toList();

    int userIndex = 0;
    for (Partner partner : approvedPartners) {
      Set<User> staff = new HashSet<>();
      // Each partner gets 1-2 internal staff members
      int staffCount = (userIndex % 3 == 0) ? 2 : 1;
      for (int i = 0; i < staffCount && userIndex < regularUsers.size(); i++) {
        staff.add(regularUsers.get(userIndex % regularUsers.size()));
        userIndex++;
      }
      if (!staff.isEmpty()) {
        partner.setStaff(staff);
        partnerRepository.save(partner);
      }
    }
  }

  // ==================== STORES ====================
  private List<Store> createStores() {
    List<Store> stores = new ArrayList<>();
    String[][] storeInfo = {
      {"Cửa hàng Q1 - Nguyễn Huệ", "123 Nguyễn Huệ, Quận 1", "0281234567", "10.7769", "106.7009"},
      {"Cửa hàng Q1 - Lê Lợi", "789 Lê Lợi, Quận 1", "0281234568", "10.7731", "106.7012"},
      {"Cửa hàng Q1 - Pasteur", "100 Pasteur, Quận 1", "0281234569", "10.7800", "106.6950"},
      {"Cửa hàng Q3 - Võ Văn Tần", "456 Võ Văn Tần, Quận 3", "0282345678", "10.7756", "106.6863"},
      {
        "Cửa hàng Q3 - Nguyễn Đình Chiểu",
        "200 Nguyễn Đình Chiểu, Quận 3",
        "0282345679",
        "10.7780",
        "106.6900"
      },
      {
        "Cửa hàng Q7 - Nguyễn Văn Linh",
        "789 Nguyễn Văn Linh, Quận 7",
        "0283456789",
        "10.7350",
        "106.7220"
      },
      {
        "Cửa hàng Q7 - Phú Mỹ Hưng", "100 Tôn Dật Tiên, Quận 7", "0283456790", "10.7280", "106.7150"
      },
      {"Cửa hàng Q2 - Thảo Điền", "50 Thảo Điền, Quận 2", "0284567890", "10.8000", "106.7350"},
      {"Cửa hàng Q9 - Lê Văn Việt", "200 Lê Văn Việt, Quận 9", "0285678901", "10.8400", "106.7800"},
      {
        "Cửa hàng Bình Thạnh - Xô Viết Nghệ Tĩnh",
        "300 Xô Viết Nghệ Tĩnh, Bình Thạnh",
        "0286789012",
        "10.8050",
        "106.7100"
      },
      {
        "Cửa hàng Tân Bình - Cộng Hòa",
        "150 Cộng Hòa, Tân Bình",
        "0287890123",
        "10.8120",
        "106.6400"
      },
      {
        "Cửa hàng Gò Vấp - Quang Trung",
        "400 Quang Trung, Gò Vấp",
        "0288901234",
        "10.8380",
        "106.6650"
      },
      {
        "Cửa hàng Phú Nhuận - Phan Xích Long",
        "200 Phan Xích Long, Phú Nhuận",
        "0289012345",
        "10.7980",
        "106.6850"
      },
      {
        "Cửa hàng Q4 - Nguyễn Tất Thành",
        "500 Nguyễn Tất Thành, Quận 4",
        "0280123456",
        "10.7580",
        "106.7050"
      },
      {
        "Cửa hàng Q5 - Trần Hưng Đạo",
        "600 Trần Hưng Đạo, Quận 5",
        "0281234560",
        "10.7550",
        "106.6750"
      },
      {"Cửa hàng Q6 - Hậu Giang", "250 Hậu Giang, Quận 6", "0282345670", "10.7450", "106.6350"},
      {
        "Cửa hàng Q8 - Phạm Thế Hiển",
        "350 Phạm Thế Hiển, Quận 8",
        "0283456780",
        "10.7200",
        "106.6600"
      },
      {"Cửa hàng Q10 - 3 Tháng 2", "450 3 Tháng 2, Quận 10", "0284567891", "10.7700", "106.6700"},
      {
        "Cửa hàng Q11 - Lạc Long Quân",
        "550 Lạc Long Quân, Quận 11",
        "0285678902",
        "10.7650",
        "106.6450"
      },
      {
        "Cửa hàng Thủ Đức - Võ Văn Ngân",
        "650 Võ Văn Ngân, Thủ Đức",
        "0286789013",
        "10.8500",
        "106.7700"
      }
    };

    for (int i = 0; i < storeInfo.length; i++) {
      String[] info = storeInfo[i];
      Partner partner = allPartners.get(i % allPartners.size());
      StoreStatus status = i == 2 || i == 16 ? StoreStatus.INACTIVE : StoreStatus.ACTIVE;
      stores.add(
          storeRepository.save(
              Store.builder()
                  .name(info[0])
                  .address(info[1] + ", TP.HCM")
                  .contactPhone(info[2])
                  .latitude(Double.parseDouble(info[3]))
                  .longitude(Double.parseDouble(info[4]))
                  .status(status)
                  .description(i < 5 ? "Cửa hàng " + (i % 2 == 0 ? "flagship" : "chi nhánh") : null)
                  .partner(partner)
                  .build()));
    }
    return stores;
  }

  // ==================== LOCKERS & BOXES ====================
  private List<Locker> createLockers() {
    List<Locker> lockers = new ArrayList<>();
    BoxSize[][] boxConfigs = {
      {
        BoxSize.SMALL,
        BoxSize.SMALL,
        BoxSize.MEDIUM,
        BoxSize.MEDIUM,
        BoxSize.LARGE,
        BoxSize.EXTRA_LARGE
      },
      {
        BoxSize.SMALL,
        BoxSize.MEDIUM,
        BoxSize.MEDIUM,
        BoxSize.LARGE,
        BoxSize.LARGE,
        BoxSize.EXTRA_LARGE
      },
      {BoxSize.SMALL, BoxSize.MEDIUM, BoxSize.LARGE, BoxSize.EXTRA_LARGE},
      {BoxSize.SMALL, BoxSize.SMALL, BoxSize.MEDIUM, BoxSize.MEDIUM, BoxSize.LARGE}
    };
    LockerStatus[] statuses = {
      LockerStatus.ACTIVE, LockerStatus.ACTIVE, LockerStatus.MAINTENANCE, LockerStatus.DISCONNECTED
    };

    // Create 20 lockers across stores
    for (int i = 0; i < 20; i++) {
      Store store = allStores.get(i % allStores.size());
      String code = String.format("LOC-%02d-%03d", (i % 12) + 1, i + 1);
      String name = "Tủ " + (char) ('A' + (i % 4)) + " - Store " + ((i % allStores.size()) + 1);
      LockerStatus status = statuses[i % statuses.length];
      BoxSize[] sizes = boxConfigs[i % boxConfigs.length];
      lockers.add(createLockerWithBoxes(code, name, store, status, sizes));
    }

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

    // ===== More orders with boxes for junction tables (order_send_boxes, order_receive_boxes)
    // =====
    // Get more boxes from different lockers
    List<Box> locker2Boxes = boxRepository.findByLockerId(locker2.getId());
    List<Box> locker3Boxes = boxRepository.findByLockerId(locker3.getId());

    // Orders with sendBoxes (10 more)
    for (int i = 0; i < 10 && i < locker2Boxes.size(); i++) {
      Box sendBox = locker2Boxes.get(i);
      sendBox.setStatus(BoxStatus.OCCUPIED);
      boxRepository.save(sendBox);

      User customer = allUsers.get(6 + (i % 10)); // customers
      orders.add(
          createFullOrder(
              customer,
              locker2,
              sendBox,
              null,
              i % 2 == 0 ? OrderStatus.WAITING : OrderStatus.COLLECTED,
              String.format("%06d", 500000 + i),
              i % 2 == 0 ? null : 1.5 + (i * 0.3),
              25000 + (i * 5000),
              "Đơn hàng tự động #" + (i + 1),
              -i,
              null,
              storage,
              laundry));
    }

    // Orders with receiveBoxes (10 more) - for completed/returned orders
    for (int i = 0; i < 10 && i < locker3Boxes.size(); i++) {
      Box receiveBox = locker3Boxes.get(i);
      receiveBox.setStatus(BoxStatus.OCCUPIED);
      boxRepository.save(receiveBox);

      User customer = allUsers.get(6 + ((i + 5) % 10)); // different customers
      orders.add(
          createFullOrder(
              customer,
              locker3,
              null,
              receiveBox,
              i % 3 == 0 ? OrderStatus.COMPLETED : OrderStatus.RETURNED,
              String.format("%06d", 600000 + i),
              2.0 + (i * 0.2),
              35000 + (i * 3000),
              "Đơn hoàn thành #" + (i + 1),
              -(10 + i),
              i % 3 == 0 ? -(8 + i) : null,
              storage,
              premium));
    }

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

    // Prepare send/receive box sets for junction tables
    Set<Box> sendBoxes = new HashSet<>();
    Set<Box> receiveBoxes = new HashSet<>();

    if (sendBox != null) {
      sendBoxes.add(sendBox);
    }
    if (receiveBox != null) {
      receiveBoxes.add(receiveBox);
    }

    Order order =
        orderRepository.save(
            Order.builder()
                .type(OrderType.LAUNDRY)
                .sender(sender)
                .locker(locker)
                .sendBox(sendBox)
                .receiveBox(receiveBox)
                .sendBoxes(sendBoxes)
                .receiveBoxes(receiveBoxes)
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
  private List<Payment> createPayments() {
    List<Payment> payments = new ArrayList<>();
    PaymentMethod[] methods = {
      PaymentMethod.VNPAY, PaymentMethod.MOMO, PaymentMethod.CASH, PaymentMethod.BANK_TRANSFER
    };
    PaymentStatus[] statuses = {
      PaymentStatus.COMPLETED, PaymentStatus.COMPLETED, PaymentStatus.PENDING, PaymentStatus.FAILED
    };

    for (int i = 0; i < Math.min(20, allOrders.size()); i++) {
      Order order = allOrders.get(i);
      PaymentMethod method = methods[i % methods.length];
      PaymentStatus status = i < 15 ? PaymentStatus.COMPLETED : statuses[i % statuses.length];

      Payment payment =
          paymentRepository.save(
              Payment.builder()
                  .order(order)
                  .customer(order.getSender())
                  .amount(order.getTotalPrice())
                  .method(method)
                  .status(status)
                  .referenceId(
                      "PAY-" + order.getId() + "-" + System.currentTimeMillis() % 100000 + "-" + i)
                  .referenceTransactionId(
                      status == PaymentStatus.COMPLETED ? "TXN" + (100000 + i) : null)
                  .description("Thanh toán đơn hàng #" + order.getId())
                  .url(
                      status == PaymentStatus.PENDING
                          ? "https://sandbox.vnpayment.vn/paymentv2/..."
                          : null)
                  .build());
      payments.add(payment);
    }
    return payments;
  }

  // ==================== ACCESS CODES ====================
  private int createAccessCodes() {
    int count = 0;
    AccessCodeAction[] actions = {AccessCodeAction.COLLECT, AccessCodeAction.RETURN};
    AccessCodeStatus[] statuses = {
      AccessCodeStatus.ACTIVE, AccessCodeStatus.USED, AccessCodeStatus.EXPIRED
    };
    String[] staffNames = {"Nguyễn Văn Tùng", "Trần Thị Hiền", "Lê Văn Tài", "Phạm Thị Mai"};

    for (int i = 0; i < 20; i++) {
      Partner partner = allPartners.get(i % allPartners.size());
      Order order = allOrders.get(i % allOrders.size());
      AccessCodeAction action = actions[i % actions.length];
      AccessCodeStatus status = statuses[i % statuses.length];
      String code =
          String.format("%s%05d", action == AccessCodeAction.COLLECT ? "COL" : "RET", i + 1);

      accessCodeRepository.save(
          StaffAccessCode.builder()
              .code(code)
              .order(order)
              .partner(partner)
              .action(action)
              .status(status)
              .expiresAt(
                  status == AccessCodeStatus.EXPIRED
                      ? LocalDateTime.now().minusDays(1)
                      : LocalDateTime.now().plusHours(24))
              .usedAt(status == AccessCodeStatus.USED ? LocalDateTime.now().minusHours(i) : null)
              .staffName(status == AccessCodeStatus.USED ? staffNames[i % staffNames.length] : null)
              .notes(
                  "Mã "
                      + (action == AccessCodeAction.COLLECT ? "lấy đồ" : "trả đồ")
                      + " #"
                      + (i + 1))
              .build());
      count++;
    }
    return count;
  }

  // ==================== LOYALTY ====================
  private int createLoyaltyAccounts() {
    int count = 0;
    for (int i = 6; i < Math.min(26, allUsers.size()); i++) {
      User user = allUsers.get(i);
      long points = (long) ((i - 5) * 150 + Math.random() * 500);
      long redeemed = (long) (Math.random() * points * 0.3);
      BigDecimal spent = new BigDecimal((i - 5) * 100000 + (int) (Math.random() * 500000));

      loyaltyAccountRepository.save(
          LoyaltyAccount.builder()
              .user(user)
              .pointsBalance(points - redeemed)
              .totalPointsEarned(points)
              .totalPointsRedeemed(redeemed)
              .totalAmountSpent(spent)
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

    // Add more notifications to reach 20+
    // Using valid notification types that exist in DB constraint
    NotificationType[] validTypes = {
      NotificationType.SYSTEM,
      NotificationType.ORDER_STATUS,
      NotificationType.PAYMENT,
      NotificationType.PICKUP_REMINDER
    };
    String[] titles = {
      "Thông báo hệ thống", "Cập nhật đơn hàng", "Thông tin thanh toán", "Nhắc nhở lấy đồ"
    };
    String[] messages = {
      "Hệ thống đã được nâng cấp",
      "Đơn hàng của bạn đã được cập nhật",
      "Thanh toán đã được xử lý",
      "Đồ của bạn đã sẵn sàng để lấy"
    };

    for (int i = 0; i < 10; i++) {
      User user = allUsers.get((i + 8) % allUsers.size());
      int typeIndex = i % validTypes.length;
      notificationRepository.save(
          Notification.builder()
              .user(user)
              .type(validTypes[typeIndex])
              .title(titles[typeIndex])
              .message(messages[typeIndex])
              .status(i < 5 ? NotificationStatus.READ : NotificationStatus.UNREAD)
              .build());
      count++;
    }

    return count;
  }

  // ==================== ORDER STATUS HISTORY ====================
  private int createOrderStatusHistory() {
    int count = 0;
    String[] actorTypes = {"CUSTOMER", "STAFF", "SYSTEM", "PARTNER", "ADMIN"};

    for (int i = 0; i < Math.min(20, allOrders.size()); i++) {
      Order order = allOrders.get(i);
      OrderStatus[] statusFlow =
          switch (order.getStatus()) {
            case COMPLETED ->
                new OrderStatus[] {
                  OrderStatus.INITIALIZED,
                  OrderStatus.WAITING,
                  OrderStatus.COLLECTED,
                  OrderStatus.PROCESSING,
                  OrderStatus.READY,
                  OrderStatus.RETURNED,
                  OrderStatus.COMPLETED
                };
            case CANCELED ->
                new OrderStatus[] {
                  OrderStatus.INITIALIZED, OrderStatus.WAITING, OrderStatus.CANCELED
                };
            case RETURNED ->
                new OrderStatus[] {
                  OrderStatus.INITIALIZED, OrderStatus.WAITING, OrderStatus.COLLECTED,
                  OrderStatus.PROCESSING, OrderStatus.READY, OrderStatus.RETURNED
                };
            default -> new OrderStatus[] {OrderStatus.INITIALIZED, order.getStatus()};
          };

      LocalDateTime timestamp = order.getCreatedAt();
      for (int j = 0; j < statusFlow.length; j++) {
        String fromStatus = j == 0 ? null : statusFlow[j - 1].name();
        orderStatusHistoryRepository.save(
            OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(statusFlow[j].name())
                .changedAt(timestamp.plusHours(j * 2))
                .changedBy(
                    j == 0
                        ? order.getSender().getId()
                        : (j % 2 == 0 ? 1L : order.getSender().getId()))
                .actorType(actorTypes[j % actorTypes.length])
                .actorName(j == 0 ? order.getSender().getName() : "System")
                .note(
                    "Trạng thái chuyển từ "
                        + (fromStatus != null ? fromStatus : "N/A")
                        + " sang "
                        + statusFlow[j].name())
                .build());
        count++;
      }
    }
    return count;
  }

  // ==================== ORDER RATINGS ====================
  private int createOrderRatings() {
    int count = 0;
    List<Order> completedOrders =
        allOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).toList();

    String[] comments = {
      "Dịch vụ rất tốt, đồ giặt sạch sẽ thơm tho!",
      "Nhân viên phục vụ nhiệt tình, giao hàng đúng hẹn",
      "Tốt nhưng cần cải thiện thời gian xử lý",
      "Giá cả hợp lý, chất lượng ổn",
      "Lần đầu sử dụng, rất hài lòng",
      "Đồ giặt sạch, đóng gói cẩn thận",
      "Tuyệt vời! Sẽ tiếp tục sử dụng",
      "Dịch vụ nhanh chóng, tiện lợi",
      "Khá tốt, giá hơi cao",
      "Rất tiện lợi cho người bận rộn"
    };

    for (int i = 0; i < Math.min(20, completedOrders.size() * 2); i++) {
      Order order = completedOrders.get(i % completedOrders.size());
      User user = order.getSender();
      int rating = 3 + (i % 3); // 3-5 stars

      // Check if rating already exists
      if (orderRatingRepository.existsByOrderIdAndUserId(order.getId(), user.getId())) {
        continue;
      }

      orderRatingRepository.save(
          OrderRating.builder()
              .order(order)
              .user(user)
              .rating(rating)
              .comment(comments[i % comments.length])
              .serviceRating(rating)
              .speedRating(rating - (i % 2))
              .staffRating(Math.min(5, rating + (i % 2)))
              .isVisible(true)
              .build());
      count++;
    }
    return count;
  }

  // ==================== REFUNDS ====================
  private int createRefunds() {
    int count = 0;
    Refund.RefundStatus[] statuses = {
      Refund.RefundStatus.PENDING, Refund.RefundStatus.COMPLETED,
      Refund.RefundStatus.PROCESSING, Refund.RefundStatus.FAILED
    };
    String[] reasons = {
      "Khách hàng yêu cầu hủy đơn",
      "Đơn hàng bị hư hỏng",
      "Giao hàng trễ quá quy định",
      "Sai dịch vụ",
      "Lỗi thanh toán trùng"
    };

    for (int i = 0; i < 20; i++) {
      Payment payment = allPayments.get(i % allPayments.size());
      BigDecimal refundAmount =
          payment.getAmount().multiply(new BigDecimal("0." + (30 + (i % 70))));

      refundRepository.save(
          Refund.builder()
              .payment(payment)
              .order(payment.getOrder())
              .amount(refundAmount)
              .status(statuses[i % statuses.length])
              .reason(reasons[i % reasons.length])
              .transactionId("REF-" + System.currentTimeMillis() + "-" + i)
              .requestedAt(LocalDateTime.now().minusDays(i))
              .processedAt(i % 2 == 0 ? LocalDateTime.now().minusDays(i - 1) : null)
              .processedBy(i % 2 == 0 ? allUsers.get(0) : null)
              .notes("Yêu cầu hoàn tiền #" + (i + 1))
              .build());
      count++;
    }
    return count;
  }

  // ==================== PROMOTIONS ====================
  private List<Promotion> createPromotions() {
    List<Promotion> promotions = new ArrayList<>();
    String[][] promoData = {
      {"WELCOME10", "Giảm 10% đơn đầu", "PERCENTAGE", "10", "50000", "0"},
      {"SAVE20K", "Giảm 20.000đ", "FIXED_AMOUNT", "20000", "100000", "1000"},
      {"VIP30", "VIP giảm 30%", "PERCENTAGE", "30", "100000", "500"},
      {"NEWUSER", "Người dùng mới", "FIXED_AMOUNT", "15000", "50000", "2000"},
      {"SUMMER25", "Hè giảm 25%", "PERCENTAGE", "25", "80000", "800"},
      {"FLASH50", "Flash Sale 50%", "PERCENTAGE", "50", "150000", "100"},
      {"LOYAL15", "Khách thân thiết", "PERCENTAGE", "15", "30000", "1500"},
      {"WEEKEND", "Cuối tuần vui vẻ", "FIXED_AMOUNT", "10000", "40000", "3000"},
      {"BIRTHDAY", "Sinh nhật", "PERCENTAGE", "20", "0", "0"},
      {"REFERRAL", "Giới thiệu bạn", "FIXED_AMOUNT", "25000", "80000", "5000"}
    };

    for (int i = 0; i < 20; i++) {
      String[] data = promoData[i % promoData.length];
      String code = i < 10 ? data[0] : data[0] + "_" + (i - 9);

      Promotion promo =
          promotionRepository.save(
              Promotion.builder()
                  .code(code)
                  .title(data[1])
                  .description("Chương trình khuyến mãi: " + data[1])
                  .discountType(Promotion.DiscountType.valueOf(data[2]))
                  .discountValue(new BigDecimal(data[3]))
                  .maxDiscountAmount(
                      new BigDecimal(data[3]).compareTo(new BigDecimal("100")) > 0
                          ? null
                          : new BigDecimal("50000"))
                  .minOrderAmount(new BigDecimal(data[4]))
                  .startDate(LocalDateTime.now().minusMonths(i % 3))
                  .endDate(LocalDateTime.now().plusMonths(i % 6 + 1))
                  .totalUsageLimit(Integer.parseInt(data[5]) > 0 ? Integer.parseInt(data[5]) : null)
                  .perUserLimit(i % 3 + 1)
                  .isActive(i < 15)
                  .priority(10 - (i % 10))
                  .acquisitionType(
                      i < 15 ? Promotion.AcquisitionType.CODE : Promotion.AcquisitionType.POINTS)
                  .pointsRequired(i >= 15 ? (i - 14) * 100 : null)
                  .build());
      promotions.add(promo);
    }
    return promotions;
  }

  // ==================== PROMOTION USAGE ====================
  private int createPromotionUsage() {
    int count = 0;
    for (int i = 0; i < 20; i++) {
      Promotion promo = allPromotions.get(i % allPromotions.size());
      User user = allUsers.get((i + 6) % allUsers.size()); // Customers
      Order order = allOrders.get(i % allOrders.size());

      promotionUsageRepository.save(
          PromotionUsage.builder()
              .promotion(promo)
              .user(user)
              .order(order)
              .discountApplied(promo.getDiscountValue())
              .usedAt(LocalDateTime.now().minusDays(i))
              .usageType(
                  i < 15
                      ? PromotionUsage.UsageType.PROMO_CODE
                      : PromotionUsage.UsageType.POINTS_REDEMPTION)
              .pointsSpent(i >= 15 ? (i - 14) * 50 : null)
              .rewardCode(i >= 15 ? "RWD-" + System.currentTimeMillis() % 10000 + "-" + i : null)
              .status(
                  i % 4 == 0 ? PromotionUsage.UsageStatus.EXPIRED : PromotionUsage.UsageStatus.USED)
              .build());
      count++;
    }
    return count;
  }

  // ==================== AUDIT LOGS ====================
  private int createAuditLogs() {
    int count = 0;
    AuditLog.AuditAction[] actions = {
      AuditLog.AuditAction.LOGIN, AuditLog.AuditAction.LOGOUT,
      AuditLog.AuditAction.ORDER_CREATED, AuditLog.AuditAction.ORDER_UPDATED,
      AuditLog.AuditAction.PAYMENT_COMPLETED, AuditLog.AuditAction.CREATE,
      AuditLog.AuditAction.UPDATE, AuditLog.AuditAction.DELETE
    };
    String[] entityTypes = {"USER", "ORDER", "PAYMENT", "STORE", "LOCKER", "PARTNER"};
    String[] ipAddresses = {"192.168.1.100", "10.0.0.50", "172.16.0.25", "192.168.0.1"};
    String[] userAgents = {
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0",
      "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0) Safari/605.1",
      "Mozilla/5.0 (Android 14; Mobile) Chrome/120.0",
      "PostmanRuntime/7.35.0"
    };

    for (int i = 0; i < 25; i++) {
      User user = allUsers.get(i % allUsers.size());
      AuditLog.AuditAction action = actions[i % actions.length];
      String entityType = entityTypes[i % entityTypes.length];

      auditLogRepository.save(
          AuditLog.builder()
              .action(action)
              .entityType(entityType)
              .entityId((long) (i + 1))
              .user(user)
              .userRole(user.getRoles().iterator().next().getName().name())
              .ipAddress(ipAddresses[i % ipAddresses.length])
              .userAgent(userAgents[i % userAgents.length])
              .description(action.name() + " on " + entityType + " #" + (i + 1))
              .timestamp(LocalDateTime.now().minusHours(i * 2))
              .status(i % 10 == 0 ? AuditLog.AuditStatus.FAILURE : AuditLog.AuditStatus.SUCCESS)
              .errorMessage(i % 10 == 0 ? "Permission denied" : null)
              .requestId("REQ-" + System.currentTimeMillis() % 100000 + "-" + i)
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
    log.info("📊 DATA SUMMARY (min 20 records per table):");
    log.info("┌────────────────────────────────────────────────────┐");
    log.info("│ Entity                  │ Count                    │");
    log.info("├────────────────────────────────────────────────────┤");
    log.info(
        "│ Permissions             │ {}                       │",
        String.format("%6d", permissionRepository.count()));
    log.info(
        "│ Roles                   │ {}                       │",
        String.format("%6d", roleRepository.count()));
    log.info(
        "│ Users                   │ {}                       │",
        String.format("%6d", userRepository.count()));
    log.info(
        "│ Partners                │ {}                       │",
        String.format("%6d", partnerRepository.count()));
    log.info(
        "│ Stores                  │ {}                       │",
        String.format("%6d", storeRepository.count()));
    log.info(
        "│ Lockers                 │ {}                       │",
        String.format("%6d", lockerRepository.count()));
    log.info(
        "│ Boxes                   │ {}                       │",
        String.format("%6d", boxRepository.count()));
    log.info(
        "│ Services                │ {}                       │",
        String.format("%6d", serviceRepository.count()));
    log.info(
        "│ Orders                  │ {}                       │",
        String.format("%6d", orderRepository.count()));
    log.info(
        "│ Order Details           │ {}                       │",
        String.format("%6d", orderDetailRepository.count()));
    log.info(
        "│ Order Status History    │ {}                       │",
        String.format("%6d", orderStatusHistoryRepository.count()));
    log.info(
        "│ Order Ratings           │ {}                       │",
        String.format("%6d", orderRatingRepository.count()));
    log.info(
        "│ Payments                │ {}                       │",
        String.format("%6d", paymentRepository.count()));
    log.info(
        "│ Refunds                 │ {}                       │",
        String.format("%6d", refundRepository.count()));
    log.info(
        "│ Access Codes            │ {}                       │",
        String.format("%6d", accessCodeRepository.count()));
    log.info(
        "│ Promotions              │ {}                       │",
        String.format("%6d", promotionRepository.count()));
    log.info(
        "│ Promotion Usage         │ {}                       │",
        String.format("%6d", promotionUsageRepository.count()));
    log.info(
        "│ Loyalty Accounts        │ {}                       │",
        String.format("%6d", loyaltyAccountRepository.count()));
    log.info(
        "│ Point Transactions      │ {}                       │",
        String.format("%6d", pointTransactionRepository.count()));
    log.info(
        "│ Stamp Cards             │ {}                       │",
        String.format("%6d", stampCardRepository.count()));
    log.info(
        "│ Stamp Transactions      │ {}                       │",
        String.format("%6d", stampTransactionRepository.count()));
    log.info(
        "│ Notifications           │ {}                       │",
        String.format("%6d", notificationRepository.count()));
    log.info(
        "│ Audit Logs              │ {}                       │",
        String.format("%6d", auditLogRepository.count()));
    log.info("└────────────────────────────────────────────────────┘");
    log.info("");
    log.info("🔐 TEST ACCOUNTS (password: {}):", DEFAULT_PASSWORD);
    log.info("   Admin:    baohuy2k12k4@gmail.com");
    log.info("   Partners: partner.minh@gmail.com, partner.huong@gmail.com, ...");
    log.info("   Staff:    staff.tung@gmail.com, staff.hien@gmail.com, staff.tai@gmail.com");
    log.info("   Customers: customer.huy@gmail.com, customer.lan@gmail.com, ...");
  }
}
