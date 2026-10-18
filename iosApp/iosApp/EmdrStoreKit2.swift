import Foundation
import StoreKit
import composeApp

enum EmdrStoreKit2 {
    static func install() {
        IosStoreKit2.shared.purchaseHost = PurchaseHost()
        IosStoreKit2.shared.catalog = CatalogHost()
        IosStoreKit2.shared.restorer = RestoreHost()
    }

    private final class PurchaseHost: IosStoreKit2Host {
        func purchase(sku: String, done: @escaping (String) -> Void) {
            Task {
                let outcome = await EmdrStoreKit2.purchase(sku: sku)
                await MainActor.run { done(outcome) }
            }
        }
    }

    private final class CatalogHost: IosStoreKit2Catalog {
        func prices(skus: [String], done: @escaping ([String: String]) -> Void) {
            Task {
                let loaded = await EmdrStoreKit2.prices(for: skus)
                await MainActor.run { done(loaded) }
            }
        }
    }

    private final class RestoreHost: IosStoreKit2Restorer {
        func finishOutstanding(skus: [String], done: @escaping (KotlinBoolean) -> Void) {
            Task {
                let any = await EmdrStoreKit2.finishOutstanding(skus: skus)
                await MainActor.run { done(KotlinBoolean(value: any)) }
            }
        }
    }

    /// StoreKit 2 purchase sheet. No appAccountToken — we never attach an identity.
    private static func purchase(sku: String) async -> String {
        do {
            let products = try await Product.products(for: [sku])
            guard let product = products.first else { return "unavailable" }
            switch try await product.purchase() {
            case .success(let verification):
                let transaction = try verified(verification)
                await transaction.finish()
                return "success"
            case .userCancelled:
                return "cancelled"
            case .pending:
                return "pending"
            @unknown default:
                return "failed"
            }
        } catch {
            return "failed"
        }
    }

    private static func prices(for skus: [String]) async -> [String: String] {
        do {
            let products = try await Product.products(for: Set(skus))
            return Dictionary(uniqueKeysWithValues: products.map { ($0.id, $0.displayPrice) })
        } catch {
            return [:]
        }
    }

    private static func finishOutstanding(skus: [String]) async -> Bool {
        var any = false
        for sku in skus {
            guard let result = await Transaction.latest(for: sku) else { continue }
            do {
                let transaction = try verified(result)
                await transaction.finish()
                any = true
            } catch {
                continue
            }
        }
        return any
    }

    private static func verified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified:
            throw StoreKitError.notAvailableInStorefront
        case .verified(let value):
            return value
        }
    }
}
