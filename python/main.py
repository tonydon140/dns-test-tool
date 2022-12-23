import sys

from PySide6.QtCore import QThreadPool, QUrl
from PySide6.QtGui import QAction, QDesktopServices
from PySide6.QtWidgets import QApplication, QWidget, QVBoxLayout, QPushButton, QTableWidget, QTableWidgetItem, \
    QHeaderView, QHBoxLayout, QMainWindow, QMessageBox

from runnable import MySignal, CheckUpdateRunnable, TestDelayRunnable, GetDnsListRunnable


class StarMovie(QMainWindow):
    def __init__(self):
        super().__init__()
        self.resize(640, 520)
        self.setWindowTitle('DNS延迟测试工具 v0.2.0')
        self._create_menu_bar()

        # 成员变量
        self.VERSION_NUMBER = 4
        self.count = 0
        self.dns_list = []
        self.pool = QThreadPool.globalInstance()

        # 自定义信号处理
        self.signal = MySignal()
        self.signal.dns_item.connect(self.ui_delay)
        self.signal.dns_list.connect(self.ui_dns_list)
        self.signal.check_update.connect(self.ui_update)

        # 布局设置
        self.central_widget = QWidget()
        self.VL_0 = QVBoxLayout(self)
        self.HL_1 = QHBoxLayout(self)
        self.PB_flush_dns_list = QPushButton('刷新DNS列表', self)
        self.PB_test_delay = QPushButton('测试延迟', self)
        self.HL_1.addWidget(self.PB_flush_dns_list)
        self.HL_1.addWidget(self.PB_test_delay)
        self.VL_0.addLayout(self.HL_1)
        self.table = QTableWidget(self)
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        self.table.setSortingEnabled(True)
        self.VL_0.addWidget(self.table)
        self.central_widget.setLayout(self.VL_0)
        self.setCentralWidget(self.central_widget)

        # 布置信号
        self.PB_flush_dns_list.clicked.connect(self.flush_dns_list)
        self.PB_test_delay.clicked.connect(self.start_test_delay)

        self.flush_dns_list()

    def _create_menu_bar(self):
        menu = self.menuBar().addMenu('帮助')
        self.Act_update = QAction('检查更新', self)
        self.Act_update.triggered.connect(self.check_update)
        menu.addAction(self.Act_update)

    def check_update(self):
        task = CheckUpdateRunnable(self.signal.check_update)
        self.pool.start(task)

    def flush_dns_list(self):
        self.table.clearContents()
        runnable = GetDnsListRunnable(self.signal.dns_list)
        self.pool.start(runnable)

    def start_test_delay(self):
        # 1. 清空延迟显示
        self.table.clearContents()
        self.table.setSortingEnabled(False)
        self.ui_dns_list(self.dns_list)

        # 2. 测试延迟
        self.count = 0
        for i in range(len(self.dns_list)):
            dns = self.dns_list[i]
            task = TestDelayRunnable(i, dns['ip'], self.signal.dns_item)
            self.pool.start(task)

    def ui_dns_list(self, dns_list):
        self.dns_list = dns_list
        self.table.setColumnCount(4)
        self.table.setRowCount(len(self.dns_list))
        self.table.setHorizontalHeaderLabels(["描述", "DNS地址", '延迟（ms）', "信息"])
        for i in range(len(self.dns_list)):
            dns = self.dns_list[i]
            self.table.setItem(i, 0, QTableWidgetItem(dns['description']))
            self.table.setItem(i, 1, QTableWidgetItem(dns['ip']))

    def ui_delay(self, index, success, fail, delay):
        self.count += 1

        delay = 'Timeout' if delay == 0 else f'{round(delay * 1000)}'

        self.table.setItem(index, 2, QTableWidgetItem(delay))
        self.table.setItem(index, 3, QTableWidgetItem(f'成功：{success}次，失败：{fail}次'))

        if self.count == len(self.dns_list):
            self.table.setSortingEnabled(True)

    def ui_update(self, res):
        if res['code'] != 200:
            QMessageBox.information(self, "检查更新", "网络错误！请检查网络或联系作者。")
            return

        # 解析数据，检查更新
        data = res['data']
        if data['versionNumber'] <= self.VERSION_NUMBER:
            QMessageBox.information(self, "检查更新", "当前版本已是最新版本！")
            return

        # 有新版本
        text = f"有可用的最新版本！{data['version']}\n{data['description']}\n是否前往官网下载更新？"
        btn = QMessageBox.information(self, "检查更新", text, QMessageBox.StandardButton.Ok,
                                      QMessageBox.StandardButton.No)

        # 打开浏览器
        if btn is QMessageBox.StandardButton.Ok:
            QDesktopServices.openUrl(QUrl("https://gitee.com/shuilanjiao/dns-test-tool/releases"))


if __name__ == '__main__':
    app = QApplication()
    window = StarMovie()
    window.show()
    sys.exit(app.exec())
