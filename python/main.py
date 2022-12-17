import json
import sys

import requests
from PySide6.QtCore import QObject, QThreadPool, Signal, QRunnable
from PySide6.QtWidgets import QApplication, QWidget, QVBoxLayout, QPushButton, QTableWidget, QTableWidgetItem, \
    QHeaderView
from ping3 import ping


class TestDelayRunnable(QRunnable):
    def __init__(self, index, isPrimary, ip, signal):
        super().__init__()
        self.index = index
        self.ip = ip
        self.isPrimary = isPrimary
        self.signal = signal

    def run(self) -> None:
        delay = ping(self.ip)
        self.signal.emit(self.index, self.isPrimary, delay)


class GetDnsListRunnable(QRunnable):
    def __init__(self, signal):
        super().__init__()
        self.signal = signal

    def run(self) -> None:
        res = requests.get('https://tonydon.top:6515/admin/dns/list')
        res = json.loads(res.text)
        self.signal.emit(res['data'])


class MySignal(QObject):
    dns_item = Signal(int, bool, float)
    dns_list = Signal(list)


class StarMovie(QWidget):
    def __init__(self):
        super().__init__()
        # 成员变量
        self.count = 0
        self.dns_list = []
        self.pool = QThreadPool.globalInstance()

        # 自定义信号处理
        self.signal = MySignal()
        self.signal.dns_item.connect(self.ui_delay)
        self.signal.dns_list.connect(self.ui_dns_list)

        # 布局设置
        self.setWindowTitle('DNS延迟测试工具')
        self.resize(640, 520)
        self.vbl = QVBoxLayout(self)
        self.btn_test_delay = QPushButton('测试延迟', self)
        self.btn_test_delay.clicked.connect(self.start_test_delay)
        self.vbl.addWidget(self.btn_test_delay)
        self.table = QTableWidget(self)
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        self.table.setSortingEnabled(True)
        self.vbl.addWidget(self.table)

        # 初始化逻辑
        self.get_dns_data()

    def get_dns_data(self):
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
            task = TestDelayRunnable(i, True, dns['primaryIp'], self.signal.dns_item)
            self.pool.start(task)
            task = TestDelayRunnable(i, False, dns['assistantIp'], self.signal.dns_item)
            self.pool.start(task)

    def ui_dns_list(self, dns_list):
        self.dns_list = dns_list
        self.table.setColumnCount(5)
        self.table.setRowCount(len(self.dns_list))
        self.table.setHorizontalHeaderLabels(["主IP", "副IP", "信息", '主IP延迟', '副IP延迟'])
        for i in range(len(self.dns_list)):
            dns = self.dns_list[i]
            self.table.setItem(i, 0, QTableWidgetItem(dns['primaryIp']))
            self.table.setItem(i, 1, QTableWidgetItem(dns['assistantIp']))
            self.table.setItem(i, 2, QTableWidgetItem(dns['description']))

    def ui_delay(self, index, isPrimary, delay):
        self.count += 1

        delay = 'Timeout' if delay == 0 else f'{round(delay * 1000)}ms'
        col = 3 if isPrimary else 4
        self.table.setItem(index, col, QTableWidgetItem(delay))

        if self.count == len(self.dns_list) * 2:
            self.table.setSortingEnabled(True)


if __name__ == '__main__':
    app = QApplication()
    window = StarMovie()
    window.show()
    sys.exit(app.exec())
