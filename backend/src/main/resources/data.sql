INSERT INTO USUARIOS (username, password, nombre_completo, email, rol) VALUES
('admin', '$2a$12$fn28LWqh2.Ce2.wLjsgmyeoGZaSVlpvA4MARp9hnDracpObn.nHLC', 'Administrador General de Sistemas', 'admin@insecurepay.fi.cr', 'ADMIN'),
('auditor_seguridad', '$2a$12$.yEGP49D4TXW3GAlrcXpgudKIgDJS8r/zwCKi0w6UaIxc91nOQSSG', 'Lic. Esteban Solano Chaves', 'esolano@insecurepay.fi.cr', 'AUDITOR'),
('carlos_mora', '$2a$12$rb8CKgWlUuY8iQP22lxnqeyDBchxA1gambjKWWEQoXEXkLKGCjTQS', 'Carlos Mora Morales', 'cmora@correo.cr', 'CLIENTE'),
('maria_rojas', '$2a$12$I2vRCzYKtoPRdswA3z2B1.YNJ8khBNApozBuNdh9rbWtSyDHC9A0.', 'María Rojas Soto', 'mrojas@empresa.cr', 'CLIENTE'),
('juan_perez', '$2a$12$dAQwV2spycHpb0rKblSnoeWIa.etfmf3SowgIQAtT0RdekwKfZxKC', 'Juan Pérez Delgado', 'jperez@correo.cr', 'CLIENTE');

INSERT INTO CUENTAS_BANCARIAS (usuario_id, numero_cuenta, tipo_cuenta, saldo, moneda, activa) VALUES
(1, 'CR05015202001026458900', 'CORRIENTE', 50000000.00, 'CRC', TRUE),
(3, 'CR05015202001026458901', 'AHORROS', 250000.00, 'CRC', TRUE),
(4, 'CR05015202001026458902', 'CORRIENTE', 4850000.00, 'CRC', TRUE),
(5, 'CR05015202001026458903', 'AHORROS', 75000.00, 'CRC', TRUE);

INSERT INTO TRANSACCIONES (cuenta_origen_id, cuenta_destino_id, monto, concepto_html, fecha, estado) VALUES
(2, 3, 45000.00, '<p>Pago de servicios profesionales de programacion. <span style="color: green; font-weight: bold;">Verificado</span></p>', '2026-03-01 10:15:00', 'COMPLETADA'),
(3, 4, 120000.00, '<p>Cancelacion cuota de alquiler oficina central. Factura electronica #8849.</p>', '2026-03-05 14:30:00', 'COMPLETADA'),
(3, 2, 15000.00, '<p>Reembolso almuerzo equipo de trabajo.</p>', '2026-03-12 11:20:00', 'COMPLETADA'),
(4, 2, 35000.00, '<p>Aporte fondo solidario cooperativo. <span style="color: blue;">Transferencia inmediata SINPE Movil</span></p>', '2026-03-20 09:45:00', 'COMPLETADA');
