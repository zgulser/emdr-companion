import SwiftUI
import composeApp

@main
struct iOSApp: App {
    init() {
        EmdrStoreKit2.install()
    }

    var body: some Scene {
        WindowGroup {
            ComposeRootView()
                .ignoresSafeArea(.keyboard)
        }
    }
}

private struct ComposeRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
