-- =====================================================
-- LAUNDRY LOCKER SYSTEM - SAMPLE DATA (PostgreSQL)
-- Version: 1.0
-- Date: 2026-01-24
-- Description: Complete sample data for testing all business flows
-- =====================================================

-- =====================================================
-- 1. ROLES & PERMISSIONS
-- =====================================================
INSERT INTO laundry_locker_schema.roles (id, name, description, created_at) VALUES
(1, 'ADMIN', 'System administrator', NOW()),
(2, 'CUSTOMER', 'Regular customer', NOW()),
(3, 'PARTNER', 'Business partner', NOW()),
(4, 'STAFF', 'Staff member (legacy)', NOW())
ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description;

-- Reset sequence
SELECT setval('laundry_locker_schema.roles_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.roles));

-- =====================================================
-- 2. USERS
-- Passwords: all use "password123" (BCrypt encoded)
-- =====================================================
INSERT INTO laundry_locker_schema.users (id, email, name, first_name, last_name, password, phone_number, provider, email_verified, created_at) VALUES
-- Admin
(1, 'admin@laundrylocker.com', 'Admin', 'System', 'Admin', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqHqPKb6PBDYHS.8FYuN.xJmFJyGe', '0900000001', 'LOCAL', true, NOW()),
-- Partners (owners)
(2, 'partner.minh@gmail.com', 'Nguyễn Văn Minh', 'Minh', 'Nguyễn Văn', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqHqPKb6PBDYHS.8FYuN.xJmFJyGe', '0900000002', 'LOCAL', true, NOW()),
(3, 'partner.huong@gmail.com', 'Trần Thị Hương', 'Hương', 'Trần Thị', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqHqPKb6PBDYHS.8FYuN.xJmFJyGe', '0900000003', 'LOCAL', true, NOW()),
-- Customers
(4, 'customer.huy@gmail.com', 'Lê Văn Huy', 'Huy', 'Lê Văn', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqHqPKb6PBDYHS.8FYuN.xJmFJyGe', '0900000004', 'LOCAL', true, NOW()),
(5, 'customer.lan@gmail.com', 'Phạm Thị Lan', 'Lan', 'Phạm Thị', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqHqPKb6PBDYHS.8FYuN.xJmFJyGe', '0900000005', 'LOCAL', true, NOW()),
(6, 'customer.tuan@gmail.com', 'Võ Minh Tuấn', 'Tuấn', 'Võ Minh', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqHqPKb6PBDYHS.8FYuN.xJmFJyGe', '0900000006', 'LOCAL', true, NOW()),
(7, 'customer.mai@gmail.com', 'Nguyễn Thị Mai', 'Mai', 'Nguyễn Thị', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqHqPKb6PBDYHS.8FYuN.xJmFJyGe', '0900000007', 'LOCAL', true, NOW()),
(8, 'customer.duc@gmail.com', 'Trần Văn Đức', 'Đức', 'Trần Văn', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqHqPKb6PBDYHS.8FYuN.xJmFJyGe', '0900000008', 'LOCAL', true, NOW())
ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email;

-- Reset sequence
SELECT setval('laundry_locker_schema.users_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.users));

-- Assign roles to users
INSERT INTO laundry_locker_schema.user_roles (user_id, role_id) VALUES
(1, 1), -- Admin has ADMIN role
(2, 2), (2, 3), -- Partner Minh has CUSTOMER + PARTNER role
(3, 2), (3, 3), -- Partner Hương has CUSTOMER + PARTNER role
(4, 2), -- Customer Huy
(5, 2), -- Customer Lan
(6, 2), -- Customer Tuấn
(7, 2), -- Customer Mai
(8, 2)  -- Customer Đức
ON CONFLICT (user_id, role_id) DO NOTHING;

-- =====================================================
-- 3. PARTNERS
-- =====================================================
INSERT INTO laundry_locker_schema.partners (id, user_id, business_name, business_registration_number, tax_id, business_address, contact_phone, contact_email, status, approved_at, approved_by, revenue_share_percent, created_at) VALUES
(1, 2, 'Giặt Ủi Sạch Sẽ Q1', 'BRN-001-2024', 'TAX-001-2024', '123 Nguyễn Huệ, Quận 1, TP.HCM', '0900000002', 'partner.minh@gmail.com', 'APPROVED', NOW(), 1, 70.00, NOW()),
(2, 3, 'Tiệm Giặt Hương Sắc Q3', 'BRN-002-2024', 'TAX-002-2024', '456 Võ Văn Tần, Quận 3, TP.HCM', '0900000003', 'partner.huong@gmail.com', 'APPROVED', NOW(), 1, 70.00, NOW())
ON CONFLICT (id) DO UPDATE SET business_name = EXCLUDED.business_name;

SELECT setval('laundry_locker_schema.partners_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.partners));

-- =====================================================
-- 4. STORES
-- =====================================================
INSERT INTO laundry_locker_schema.stores (id, name, address, contact_phone, latitude, longitude, open_time, close_time, is_active, partner_id, created_at) VALUES
(1, 'Cửa hàng Quận 1 - Nguyễn Huệ', '123 Nguyễn Huệ, Quận 1, TP.HCM', '0281234567', 10.7769, 106.7009, '07:00', '22:00', true, 1, NOW()),
(2, 'Cửa hàng Quận 1 - Lê Lợi', '789 Lê Lợi, Quận 1, TP.HCM', '0281234568', 10.7731, 106.7012, '07:00', '22:00', true, 1, NOW()),
(3, 'Cửa hàng Quận 3 - Võ Văn Tần', '456 Võ Văn Tần, Quận 3, TP.HCM', '0281234569', 10.7756, 106.6863, '08:00', '21:00', true, 2, NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval('laundry_locker_schema.stores_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.stores));

-- =====================================================
-- 5. LAUNDRY SERVICES
-- =====================================================
INSERT INTO laundry_locker_schema.laundry_services (id, name, description, price, max_price, unit, service_type, is_addon, is_monthly_package, estimated_hours, is_active, created_at) VALUES
-- Dịch vụ gửi hàng
(1, 'Gửi hàng thường', 'Gửi đồ vào tủ với phí cơ bản', 5000.00, NULL, 'lần', 'STORAGE', false, false, 0, true, NOW()),
(2, 'Gửi qua đêm', 'Phí phụ thu khi gửi qua đêm', 5000.00, NULL, 'đêm', 'STORAGE', true, false, 0, true, NOW()),
(3, 'Gửi nhanh 2h', 'Lấy đồ trong vòng 2 tiếng', 10000.00, NULL, 'lần', 'EXPRESS', false, false, 2, true, NOW()),
-- Dịch vụ giặt
(4, 'Giặt sấy thường', 'Giặt và sấy khô quần áo thông thường', 12000.00, 15000.00, 'kg', 'LAUNDRY', false, false, 24, true, NOW()),
(5, 'Giặt hấp cao cấp', 'Giặt hấp cho đồ cao cấp, vest, áo dài', 25000.00, 35000.00, 'món', 'LAUNDRY', false, false, 48, true, NOW()),
(6, 'Giặt đồ lớn', 'Chăn, mền, rèm cửa', 30000.00, 50000.00, 'món', 'LAUNDRY', false, false, 48, true, NOW()),
-- Gói tháng
(7, 'Gói tháng Sinh viên', 'Giặt không giới hạn cho sinh viên', 50000.00, NULL, 'tháng', 'SUBSCRIPTION', false, true, 0, true, NOW()),
(8, 'Gói tháng Shipper', 'Giặt không giới hạn cho shipper', 70000.00, NULL, 'tháng', 'SUBSCRIPTION', false, true, 0, true, NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval('laundry_locker_schema.laundry_services_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.laundry_services));

-- =====================================================
-- 6. LOCKERS & BOXES
-- =====================================================
INSERT INTO laundry_locker_schema.lockers (id, code, name, store_id, location, is_active, created_at) VALUES
(1, 'LOC-Q1-NH-01', 'Tủ A - Nguyễn Huệ', 1, 'Tầng trệt, cạnh thang máy', true, NOW()),
(2, 'LOC-Q1-NH-02', 'Tủ B - Nguyễn Huệ', 1, 'Tầng trệt, góc phải', true, NOW()),
(3, 'LOC-Q1-LL-01', 'Tủ A - Lê Lợi', 2, 'Sảnh chính', true, NOW()),
(4, 'LOC-Q3-VVT-01', 'Tủ A - Võ Văn Tần', 3, 'Trước cửa hàng', true, NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval('laundry_locker_schema.lockers_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.lockers));

-- Boxes for each locker
INSERT INTO laundry_locker_schema.boxes (id, locker_id, box_number, size, status, is_active, created_at) VALUES
-- Locker 1 (LOC-Q1-NH-01): 8 boxes
(1, 1, 1, 'SMALL', 'AVAILABLE', true, NOW()),
(2, 1, 2, 'SMALL', 'AVAILABLE', true, NOW()),
(3, 1, 3, 'MEDIUM', 'AVAILABLE', true, NOW()),
(4, 1, 4, 'MEDIUM', 'AVAILABLE', true, NOW()),
(5, 1, 5, 'MEDIUM', 'AVAILABLE', true, NOW()),
(6, 1, 6, 'LARGE', 'AVAILABLE', true, NOW()),
(7, 1, 7, 'LARGE', 'AVAILABLE', true, NOW()),
(8, 1, 8, 'EXTRA_LARGE', 'AVAILABLE', true, NOW()),
-- Locker 2 (LOC-Q1-NH-02): 6 boxes
(9, 2, 1, 'SMALL', 'AVAILABLE', true, NOW()),
(10, 2, 2, 'MEDIUM', 'AVAILABLE', true, NOW()),
(11, 2, 3, 'MEDIUM', 'AVAILABLE', true, NOW()),
(12, 2, 4, 'LARGE', 'AVAILABLE', true, NOW()),
(13, 2, 5, 'LARGE', 'AVAILABLE', true, NOW()),
(14, 2, 6, 'EXTRA_LARGE', 'AVAILABLE', true, NOW()),
-- Locker 3 (LOC-Q1-LL-01): 6 boxes
(15, 3, 1, 'SMALL', 'AVAILABLE', true, NOW()),
(16, 3, 2, 'MEDIUM', 'AVAILABLE', true, NOW()),
(17, 3, 3, 'MEDIUM', 'AVAILABLE', true, NOW()),
(18, 3, 4, 'LARGE', 'AVAILABLE', true, NOW()),
(19, 3, 5, 'LARGE', 'AVAILABLE', true, NOW()),
(20, 3, 6, 'EXTRA_LARGE', 'AVAILABLE', true, NOW()),
-- Locker 4 (LOC-Q3-VVT-01): 6 boxes
(21, 4, 1, 'SMALL', 'AVAILABLE', true, NOW()),
(22, 4, 2, 'MEDIUM', 'AVAILABLE', true, NOW()),
(23, 4, 3, 'MEDIUM', 'AVAILABLE', true, NOW()),
(24, 4, 4, 'LARGE', 'AVAILABLE', true, NOW()),
(25, 4, 5, 'LARGE', 'AVAILABLE', true, NOW()),
(26, 4, 6, 'EXTRA_LARGE', 'AVAILABLE', true, NOW())
ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status;

SELECT setval('laundry_locker_schema.boxes_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.boxes));

-- =====================================================
-- 7. LOYALTY ACCOUNTS (for customers)
-- =====================================================
INSERT INTO laundry_locker_schema.loyalty_accounts (id, user_id, total_points, available_points, tier, created_at) VALUES
(1, 4, 500, 500, 'BRONZE', NOW()),  -- Customer Huy
(2, 5, 1200, 1000, 'SILVER', NOW()), -- Customer Lan
(3, 6, 200, 200, 'BRONZE', NOW()),   -- Customer Tuấn
(4, 7, 3000, 2500, 'GOLD', NOW()),   -- Customer Mai
(5, 8, 0, 0, 'BRONZE', NOW())        -- Customer Đức (new)
ON CONFLICT (id) DO UPDATE SET total_points = EXCLUDED.total_points;

SELECT setval('laundry_locker_schema.loyalty_accounts_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.loyalty_accounts));

-- =====================================================
-- 8. SAMPLE ORDERS (Different statuses for testing)
-- =====================================================

-- Order 1: COMPLETED (Customer Huy - full flow completed)
INSERT INTO laundry_locker_schema.orders (id, type, pin_code, status, sender_id, locker_id, send_box_id, receive_box_id, actual_weight, weight_unit, reservation_fee, storage_price, total_price, description, created_at, completed_at) VALUES
(1, 'LAUNDRY', NULL, 'COMPLETED', 4, 1, NULL, NULL, 2.50, 'kg', 5000.00, 5000.00, 42500.00, 'Giặt quần áo hàng ngày', NOW() - INTERVAL '7 days', NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status;

INSERT INTO laundry_locker_schema.order_details (id, order_id, service_id, quantity, unit_price, subtotal, created_at) VALUES
(1, 1, 1, 1, 5000.00, 5000.00, NOW() - INTERVAL '7 days'),
(2, 1, 4, 2.5, 15000.00, 37500.00, NOW() - INTERVAL '7 days')
ON CONFLICT (id) DO UPDATE SET subtotal = EXCLUDED.subtotal;

-- Order 2: WAITING (Customer Lan - waiting for partner to accept)
INSERT INTO laundry_locker_schema.orders (id, type, pin_code, pin_code_issued_at, status, sender_id, locker_id, send_box_id, reservation_fee, storage_price, total_price, description, created_at) VALUES
(2, 'LAUNDRY', '123456', NOW(), 'WAITING', 5, 1, 3, 5000.00, 5000.00, 0.00, 'Giặt áo vest và quần tây', NOW() - INTERVAL '1 hour')
ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status;

-- Mark box as occupied
UPDATE laundry_locker_schema.boxes SET status = 'OCCUPIED' WHERE id = 3;

INSERT INTO laundry_locker_schema.order_details (id, order_id, service_id, quantity, unit_price, subtotal, created_at) VALUES
(3, 2, 1, 1, 5000.00, 5000.00, NOW() - INTERVAL '1 hour'),
(4, 2, 5, 2, 30000.00, 60000.00, NOW() - INTERVAL '1 hour')
ON CONFLICT (id) DO UPDATE SET subtotal = EXCLUDED.subtotal;

-- Order 3: PROCESSING (Customer Tuấn - being washed)
INSERT INTO laundry_locker_schema.orders (id, type, status, sender_id, locker_id, send_box_id, actual_weight, weight_unit, reservation_fee, storage_price, total_price, description, created_at) VALUES
(3, 'LAUNDRY', 'PROCESSING', 6, 1, NULL, 3.00, 'kg', 5000.00, 5000.00, 55000.00, 'Giặt chăn mền', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status;

INSERT INTO laundry_locker_schema.order_details (id, order_id, service_id, quantity, unit_price, subtotal, created_at) VALUES
(5, 3, 1, 1, 5000.00, 5000.00, NOW() - INTERVAL '2 days'),
(6, 3, 6, 1, 50000.00, 50000.00, NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO UPDATE SET subtotal = EXCLUDED.subtotal;

-- Order 4: RETURNED (Customer Mai - waiting for pickup)
INSERT INTO laundry_locker_schema.orders (id, type, pin_code, pin_code_issued_at, status, sender_id, locker_id, receive_box_id, actual_weight, weight_unit, reservation_fee, storage_price, total_price, description, created_at) VALUES
(4, 'LAUNDRY', '654321', NOW(), 'RETURNED', 7, 1, 6, 1.50, 'kg', 5000.00, 5000.00, 32500.00, 'Giặt đồ công sở', NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status;

-- Mark receive box as occupied
UPDATE laundry_locker_schema.boxes SET status = 'OCCUPIED' WHERE id = 6;

INSERT INTO laundry_locker_schema.order_details (id, order_id, service_id, quantity, unit_price, subtotal, created_at) VALUES
(7, 4, 1, 1, 5000.00, 5000.00, NOW() - INTERVAL '3 days'),
(8, 4, 4, 1.5, 15000.00, 22500.00, NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO UPDATE SET subtotal = EXCLUDED.subtotal;

-- Order 5: INITIALIZED (Customer Đức - just created, not dropped off yet)
INSERT INTO laundry_locker_schema.orders (id, type, pin_code, pin_code_issued_at, status, sender_id, locker_id, send_box_id, reservation_fee, storage_price, total_price, description, created_at) VALUES
(5, 'LAUNDRY', '111222', NOW(), 'INITIALIZED', 8, 2, 10, 5000.00, 5000.00, 0.00, 'Giặt quần áo thể thao', NOW() - INTERVAL '10 minutes')
ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status;

-- Mark box as reserved
UPDATE laundry_locker_schema.boxes SET status = 'RESERVED' WHERE id = 10;

INSERT INTO laundry_locker_schema.order_details (id, order_id, service_id, quantity, unit_price, subtotal, created_at) VALUES
(9, 5, 1, 1, 5000.00, 5000.00, NOW() - INTERVAL '10 minutes'),
(10, 5, 4, 1, 12000.00, 12000.00, NOW() - INTERVAL '10 minutes')
ON CONFLICT (id) DO UPDATE SET subtotal = EXCLUDED.subtotal;

SELECT setval('laundry_locker_schema.orders_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.orders));
SELECT setval('laundry_locker_schema.order_details_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.order_details));

-- =====================================================
-- 9. PAYMENTS (for completed/returned orders)
-- =====================================================
INSERT INTO laundry_locker_schema.payments (id, order_id, customer_id, amount, method, status, transaction_id, paid_at, created_at) VALUES
(1, 1, 4, 42500.00, 'VNPAY', 'SUCCESS', 'VNP123456789', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status;

SELECT setval('laundry_locker_schema.payments_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.payments));

-- =====================================================
-- 10. STAFF ACCESS CODES (sample)
-- =====================================================
INSERT INTO laundry_locker_schema.staff_access_codes (id, code, order_id, partner_id, action, status, expires_at, created_at) VALUES
(1, 'ABC12XYZ', 2, 1, 'COLLECT', 'ACTIVE', NOW() + INTERVAL '24 hours', NOW())
ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status;

SELECT setval('laundry_locker_schema.staff_access_codes_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.staff_access_codes));

-- =====================================================
-- 11. NOTIFICATIONS (sample)
-- =====================================================
INSERT INTO laundry_locker_schema.notifications (id, user_id, title, message, type, status, reference_id, reference_type, created_at) VALUES
(1, 4, 'Đơn hàng hoàn thành', 'Đơn hàng #1 đã hoàn thành. Cảm ơn bạn!', 'ORDER_STATUS', 'READ', 1, 'ORDER', NOW() - INTERVAL '5 days'),
(2, 5, 'Đơn hàng đang chờ xử lý', 'Đơn hàng #2 đang chờ nhân viên đến lấy.', 'ORDER_STATUS', 'UNREAD', 2, 'ORDER', NOW() - INTERVAL '1 hour'),
(3, 7, 'Đồ đã sẵn sàng', 'Đơn hàng #4 đã được trả về tủ. Mã PIN: 654321', 'ORDER_STATUS', 'UNREAD', 4, 'ORDER', NOW() - INTERVAL '6 hours')
ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status;

SELECT setval('laundry_locker_schema.notifications_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.notifications));

-- =====================================================
-- 12. POINT TRANSACTIONS (loyalty)
-- =====================================================
INSERT INTO laundry_locker_schema.point_transactions (id, loyalty_account_id, points, type, description, reference_id, reference_type, created_at) VALUES
(1, 1, 42, 'EARN', 'Điểm từ đơn hàng #1', 1, 'ORDER', NOW() - INTERVAL '5 days'),
(2, 4, 300, 'EARN', 'Điểm thưởng khách hàng thân thiết', NULL, 'BONUS', NOW() - INTERVAL '30 days')
ON CONFLICT (id) DO UPDATE SET points = EXCLUDED.points;

SELECT setval('laundry_locker_schema.point_transactions_id_seq', (SELECT MAX(id) FROM laundry_locker_schema.point_transactions));

-- =====================================================
-- Completion Message
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '✅ Sample data imported successfully!';
    RAISE NOTICE '📊 Created: 8 users, 2 partners, 3 stores, 4 lockers, 26 boxes, 8 services, 5 orders';
END $$;

